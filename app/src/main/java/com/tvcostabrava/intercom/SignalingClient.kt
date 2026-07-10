package com.tvcostabrava.intercom

import android.os.Handler
import android.os.Looper
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Habla el protocolo minimo del server de senializacion (server/index.js):
 * join / existing-peers / peer-joined / signal / peer-left.
 * El server nunca ve audio, solo reenvia estos mensajes JSON.
 *
 * Se reconecta solo con backoff si la conexion se cae de forma inesperada
 * (cambio de red, WiFi <-> datos moviles, perdida de cobertura, etc), para
 * que la app siga funcionando sin importar que conexion haya en cada momento.
 */
class SignalingClient(
    private val serverUrl: String,
    private val myId: String,
    private val listener: Listener,
) {
    interface Listener {
        fun onExistingPeers(peerIds: List<String>)
        fun onPeerJoined(peerId: String)
        fun onPeerLeft(peerId: String)
        fun onSignal(fromPeerId: String, data: JSONObject)
        fun onConnected()
        fun onDisconnected()
    }

    private val client = OkHttpClient.Builder()
        .pingInterval(15, TimeUnit.SECONDS)
        .build()

    private val mainHandler = Handler(Looper.getMainLooper())
    private var socket: WebSocket? = null
    private var manualDisconnect = false
    private var reconnectAttempt = 0

    fun connect() {
        manualDisconnect = false
        reconnectAttempt = 0
        mainHandler.removeCallbacksAndMessages(null)
        attemptConnect()
    }

    private fun attemptConnect() {
        Log.i(TAG, "attemptConnect() -> $serverUrl (myId=$myId)")
        val request = try {
            Request.Builder().url(serverUrl).build()
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "URL invalida: $serverUrl", e)
            listener.onDisconnected()
            return
        }
        socket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.i(TAG, "onOpen: conectado al servidor de senializacion")
                reconnectAttempt = 0
                val join = JSONObject().put("type", "join").put("id", myId)
                webSocket.send(join.toString())
                listener.onConnected()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleMessage(text)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.w(TAG, "onClosed: code=$code reason=$reason")
                listener.onDisconnected()
                scheduleReconnect()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "onFailure: ${t.message} (http=${response?.code})", t)
                listener.onDisconnected()
                scheduleReconnect()
            }
        })
    }

    private fun scheduleReconnect() {
        if (manualDisconnect) return
        reconnectAttempt++
        val delayMs = (2000L * reconnectAttempt).coerceAtMost(15_000L)
        Log.i(TAG, "scheduleReconnect en ${delayMs}ms (intento $reconnectAttempt)")
        mainHandler.postDelayed({ if (!manualDisconnect) attemptConnect() }, delayMs)
    }

    private fun handleMessage(text: String) {
        Log.d(TAG, "handleMessage: $text")
        val msg = JSONObject(text)
        when (msg.optString("type")) {
            "existing-peers" -> {
                val arr: JSONArray = msg.optJSONArray("peers") ?: JSONArray()
                val ids = (0 until arr.length()).map { arr.getString(it) }
                Log.i(TAG, "existing-peers: $ids")
                listener.onExistingPeers(ids)
            }
            "peer-joined" -> {
                Log.i(TAG, "peer-joined: ${msg.getString("id")}")
                listener.onPeerJoined(msg.getString("id"))
            }
            "peer-left" -> listener.onPeerLeft(msg.getString("id"))
            "signal" -> listener.onSignal(msg.getString("from"), msg.getJSONObject("data"))
        }
    }

    fun sendSignal(toPeerId: String, data: JSONObject) {
        val msg = JSONObject()
            .put("type", "signal")
            .put("to", toPeerId)
            .put("data", data)
        val sent = socket?.send(msg.toString()) ?: false
        Log.d(TAG, "sendSignal to=$toPeerId kind=${data.optString("kind")} sent=$sent")
    }

    fun disconnect() {
        manualDisconnect = true
        mainHandler.removeCallbacksAndMessages(null)
        socket?.close(1000, "bye")
        socket = null
    }

    companion object {
        private const val TAG = "SignalingClient"
    }
}
