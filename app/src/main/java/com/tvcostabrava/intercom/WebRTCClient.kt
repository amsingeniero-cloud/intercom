package com.tvcostabrava.intercom

import android.content.Context
import org.json.JSONObject
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

/**
 * Mesh WebRTC: cada dispositivo abre una PeerConnection directa con cada otro
 * dispositivo de la sala (sin server de audio). Pensado para grupos pequenos (~6-8).
 */
class WebRTCClient(
    context: Context,
    private val signalingCallback: SignalingCallback,
) {
    interface SignalingCallback {
        fun sendSignal(toPeerId: String, data: JSONObject)
    }

    private val eglBase = EglBase.create()

    private val peerConnections = mutableMapOf<String, PeerConnection>()
    private lateinit var localAudioTrack: AudioTrack
    private lateinit var audioSource: AudioSource
    private lateinit var peerConnectionFactory: PeerConnectionFactory

    private val iceServers = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
        // TURN publico gratuito (Open Relay Project) para redes con NAT estricto / datos moviles
        PeerConnection.IceServer.builder("turn:openrelay.metered.ca:80")
            .setUsername("openrelayproject")
            .setPassword("openrelayproject")
            .createIceServer(),
        PeerConnection.IceServer.builder("turn:openrelay.metered.ca:443")
            .setUsername("openrelayproject")
            .setPassword("openrelayproject")
            .createIceServer(),
    )

    init {
        val initOptions = PeerConnectionFactory.InitializationOptions.builder(context)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(initOptions)

        peerConnectionFactory = PeerConnectionFactory.builder()
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true))
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBase.eglBaseContext))
            .createPeerConnectionFactory()

        val audioConstraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("googEchoCancellation", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googNoiseSuppression", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googAutoGainControl", "true"))
        }
        audioSource = peerConnectionFactory.createAudioSource(audioConstraints)
        localAudioTrack = peerConnectionFactory.createAudioTrack("local_audio", audioSource)
        localAudioTrack.setEnabled(false) // arranca en silencio: hay que pulsar para hablar
    }

    fun setMicEnabled(enabled: Boolean) {
        localAudioTrack.setEnabled(enabled)
    }

    /** Llamar cuando llega existing-peers o peer-joined con un id nuevo. */
    fun connectToPeer(peerId: String, isInitiator: Boolean) {
        if (peerConnections.containsKey(peerId)) return

        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }

        val pc = peerConnectionFactory.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onIceCandidate(candidate: IceCandidate) {
                val data = JSONObject()
                    .put("kind", "ice-candidate")
                    .put("sdpMid", candidate.sdpMid)
                    .put("sdpMLineIndex", candidate.sdpMLineIndex)
                    .put("candidate", candidate.sdp)
                signalingCallback.sendSignal(peerId, data)
            }

            override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>) {}
            override fun onAddStream(stream: MediaStream) {}
            override fun onRemoveStream(stream: MediaStream) {}
            override fun onDataChannel(channel: org.webrtc.DataChannel) {}
            override fun onRenegotiationNeeded() {}
            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) {}
            override fun onIceConnectionReceivingChange(receiving: Boolean) {}
            override fun onIceGatheringChange(state: PeerConnection.IceGatheringState) {}
            override fun onSignalingChange(state: PeerConnection.SignalingState) {}
            override fun onAddTrack(receiver: org.webrtc.RtpReceiver, streams: Array<out MediaStream>) {}
            override fun onTrack(transceiver: org.webrtc.RtpTransceiver) {}
        }) ?: return

        pc.addTrack(localAudioTrack, listOf("local_stream"))
        peerConnections[peerId] = pc

        if (isInitiator) {
            pc.createOffer(object : SdpObserverAdapter() {
                override fun onCreateSuccess(desc: SessionDescription) {
                    pc.setLocalDescription(SdpObserverAdapter(), desc)
                    val data = JSONObject()
                        .put("kind", "sdp")
                        .put("type", desc.type.canonicalForm())
                        .put("sdp", desc.description)
                    signalingCallback.sendSignal(peerId, data)
                }
            }, MediaConstraints())
        }
    }

    /** Llamar con lo que llega de SignalingClient.onSignal. */
    fun onRemoteSignal(peerId: String, data: JSONObject) {
        val pc = peerConnections[peerId] ?: run {
            // Oferta de un peer que aun no conocemos: crear la conexion como no-iniciador.
            connectToPeer(peerId, isInitiator = false)
            peerConnections[peerId]
        } ?: return

        when (data.optString("kind")) {
            "sdp" -> {
                val type = if (data.getString("type") == "offer") {
                    SessionDescription.Type.OFFER
                } else {
                    SessionDescription.Type.ANSWER
                }
                val desc = SessionDescription(type, data.getString("sdp"))
                pc.setRemoteDescription(SdpObserverAdapter(), desc)

                if (type == SessionDescription.Type.OFFER) {
                    pc.createAnswer(object : SdpObserverAdapter() {
                        override fun onCreateSuccess(answer: SessionDescription) {
                            pc.setLocalDescription(SdpObserverAdapter(), answer)
                            val answerData = JSONObject()
                                .put("kind", "sdp")
                                .put("type", "answer")
                                .put("sdp", answer.description)
                            signalingCallback.sendSignal(peerId, answerData)
                        }
                    }, MediaConstraints())
                }
            }
            "ice-candidate" -> {
                val candidate = IceCandidate(
                    data.getString("sdpMid"),
                    data.getInt("sdpMLineIndex"),
                    data.getString("candidate"),
                )
                pc.addIceCandidate(candidate)
            }
        }
    }

    fun removePeer(peerId: String) {
        peerConnections.remove(peerId)?.close()
    }

    fun dispose() {
        peerConnections.values.forEach { it.close() }
        peerConnections.clear()
        audioSource.dispose()
        peerConnectionFactory.dispose()
        eglBase.release()
    }

    private open class SdpObserverAdapter : SdpObserver {
        override fun onCreateSuccess(desc: SessionDescription) {}
        override fun onSetSuccess() {}
        override fun onCreateFailure(error: String) {}
        override fun onSetFailure(error: String) {}
    }
}
