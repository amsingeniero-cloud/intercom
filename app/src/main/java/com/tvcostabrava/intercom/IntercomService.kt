package com.tvcostabrava.intercom

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.Network
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject
import java.util.UUID

data class IntercomState(
    val connectedToServer: Boolean = false,
    val peerCount: Int = 0,
)

class IntercomService : Service(), SignalingClient.Listener, WebRTCClient.SignalingCallback {

    inner class LocalBinder : Binder() {
        fun getService(): IntercomService = this@IntercomService
    }

    private val binder = LocalBinder()
    private val myId = UUID.randomUUID().toString().take(8)

    private lateinit var signalingClient: SignalingClient
    private lateinit var webRTCClient: WebRTCClient
    private lateinit var audioManager: AudioManager

    private var pttPressed = false
    private var handsFreeOn = false
    private val connectedPeers = mutableSetOf<String>()
    private var activeChannels: MutableSet<String> = mutableSetOf()

    private val _state = MutableStateFlow(IntercomState())
    val state: StateFlow<IntercomState> = _state

    private lateinit var connectivityManager: ConnectivityManager
    private var activeNetwork: Network? = null

    /**
     * Detecta cambios de red (WiFi <-> datos moviles, cambio de WiFi, etc) y fuerza
     * una reconexion completa para que la app siga funcionando con la conexion que
     * haya disponible en cada momento, sin tener que reabrir la app a mano.
     */
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            val previous = activeNetwork
            activeNetwork = network
            Log.i(TAG, "networkCallback.onAvailable: $network (previous=$previous)")
            if (previous != null && previous != network) {
                restartConnection(currentServerUrl())
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundNotification()

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        @Suppress("DEPRECATION")
        audioManager.isSpeakerphoneOn = true

        connectivityManager = getSystemService(ConnectivityManager::class.java)
        connectivityManager.registerDefaultNetworkCallback(networkCallback)

        SettingsStore.getRole(this)?.let { activeChannels.add(it) }

        webRTCClient = WebRTCClient(applicationContext, this)
        webRTCClient.updateMyChannels(activeChannels)
        signalingClient = SignalingClient(currentServerUrl(), myId, this)
        fetchTurnCredentialsThenConnect(currentServerUrl())
    }

    private fun currentServerUrl(): String =
        SettingsStore.getServerUrl(this).ifBlank { BuildConfig.SIGNALING_URL }

    /** El endpoint /turn-credentials vive en el mismo servidor de senializacion (wss:// -> https://). */
    private fun turnCredentialsUrl(signalingUrl: String): String {
        val httpBase = signalingUrl
            .replaceFirst("wss://", "https://")
            .replaceFirst("ws://", "http://")
            .trimEnd('/')
        return "$httpBase/turn-credentials"
    }

    /**
     * Pide las credenciales TURN reales antes de conectar la senializacion, para que
     * ya esten listas cuando lleguen los primeros peers y haya que negociar audio.
     * Si el servidor no responde a tiempo (timeout 4s) o falla, sigue solo con STUN.
     */
    private fun fetchTurnCredentialsThenConnect(url: String) {
        TurnCredentialsFetcher.fetch(turnCredentialsUrl(url)) { turnServers ->
            webRTCClient.updateIceServers(turnServers)
            signalingClient.connect()
        }
    }

    /**
     * Reconexion completa: cierra la senializacion, tira las conexiones WebRTC viejas
     * (atadas a la red anterior) y vuelve a unirse a la sala desde cero. Se usa tanto
     * al cambiar la URL del servidor desde Ajustes como al cambiar de red.
     */
    private fun restartConnection(url: String) {
        Log.i(TAG, "restartConnection -> $url")
        signalingClient.disconnect()
        webRTCClient.dispose()
        connectedPeers.clear()
        publishState()

        webRTCClient = WebRTCClient(applicationContext, this)
        webRTCClient.updateMyChannels(activeChannels)
        applyMicState()

        signalingClient = SignalingClient(url, myId, this)
        fetchTurnCredentialsThenConnect(url)
    }

    /** Llamado desde Ajustes cuando el usuario guarda una URL nueva: reconecta al vuelo. */
    fun updateServerUrl(newUrl: String) {
        SettingsStore.setServerUrl(this, newUrl)
        restartConnection(newUrl.ifBlank { currentServerUrl() })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = binder

    // ---- Controles llamados desde la UI ----

    fun setPttPressed(pressed: Boolean) {
        pttPressed = pressed
        applyMicState()
    }

    fun setHandsFree(enabled: Boolean) {
        handsFreeOn = enabled
        applyMicState()
    }

    private fun applyMicState() {
        val enabled = pttPressed || handsFreeOn
        Log.i(TAG, "applyMicState: enabled=$enabled (pttPressed=$pttPressed, handsFreeOn=$handsFreeOn), peers=${connectedPeers.size}")
        webRTCClient.setMicEnabled(enabled)
    }

    /** Llamado desde la UI cada vez que cambia el conjunto de interruptores encendidos. */
    fun setActiveChannels(channels: Set<String>) {
        activeChannels = channels.toMutableSet()
        Log.i(TAG, "setActiveChannels -> $activeChannels")
        webRTCClient.updateMyChannels(activeChannels)
        signalingClient.sendChannels(activeChannels.toList())
    }

    // ---- WebRTCClient.SignalingCallback ----

    override fun sendSignal(toPeerId: String, data: JSONObject) {
        signalingClient.sendSignal(toPeerId, data)
    }

    // ---- SignalingClient.Listener ----

    override fun onExistingPeers(peers: List<PeerInfo>) {
        Log.i(TAG, "onExistingPeers: $peers (myId=$myId)")
        peers.forEach { peer ->
            connectedPeers.add(peer.id)
            webRTCClient.updatePeerChannels(peer.id, peer.channels.toSet())
            webRTCClient.connectToPeer(peer.id, isInitiator = true)
        }
        publishState()
    }

    override fun onPeerJoined(peerId: String) {
        Log.i(TAG, "onPeerJoined: $peerId")
        // El peer nuevo nos hara una oferta; solo la registramos, no iniciamos nosotros.
        connectedPeers.add(peerId)
        publishState()
    }

    override fun onPeerLeft(peerId: String) {
        Log.i(TAG, "onPeerLeft: $peerId")
        connectedPeers.remove(peerId)
        webRTCClient.removePeer(peerId)
        publishState()
    }

    override fun onPeerChannels(peerId: String, channels: List<String>) {
        webRTCClient.updatePeerChannels(peerId, channels.toSet())
    }

    override fun onSignal(fromPeerId: String, data: JSONObject) {
        webRTCClient.onRemoteSignal(fromPeerId, data)
    }

    override fun onConnected() {
        Log.i(TAG, "onConnected (myId=$myId)")
        _state.value = _state.value.copy(connectedToServer = true)
        if (activeChannels.isNotEmpty()) signalingClient.sendChannels(activeChannels.toList())
    }

    override fun onDisconnected() {
        Log.w(TAG, "onDisconnected")
        _state.value = _state.value.copy(connectedToServer = false)
    }

    private fun publishState() {
        _state.value = _state.value.copy(peerCount = connectedPeers.size)
    }

    // ---- Notificacion foreground ----

    private fun startForegroundNotification() {
        val channelId = "intercom_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Intercom",
                NotificationManager.IMPORTANCE_LOW,
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Intercom activo")
            .setContentText("Manten pulsado para hablar")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        val foregroundServiceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
        } else {
            0
        }

        ServiceCompat.startForeground(this, 1, notification, foregroundServiceType)
    }

    override fun onDestroy() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
        signalingClient.disconnect()
        webRTCClient.dispose()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "IntercomService"
    }
}
