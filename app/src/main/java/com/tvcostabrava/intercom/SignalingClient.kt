package com.tvcostabrava.intercom

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

    private var socket: WebSocket? = null

    fun connect() {
        val request = try {
            Request.Builder().url(serverUrl).build()
        } catch (e: IllegalArgumentException) {
            listener.onDisconnected()
            return
        }
        socket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                val join = JSONObject().put("type", "join").put("id", myId)
                webSocket.send(join.toString())
                listener.onConnected()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleMessage(text)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                listener.onDisconnected()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                listener.onDisconnected()
            }
        })
    }

    private fun handleMessage(text: String) {
        val msg = JSONObject(text)
        when (msg.optString("type")) {
            "existing-peers" -> {
                val arr: JSONArray = msg.optJSONArray("peers") ?: JSONArray()
                val ids = (0 until arr.length()).map { arr.getString(it) }
                listener.onExistingPeers(ids)
            }
            "peer-joined" -> listener.onPeerJoined(msg.getString("id"))
            "peer-left" -> listener.onPeerLeft(msg.getString("id"))
            "signal" -> listener.onSignal(msg.getString("from"), msg.getJSONObject("data"))
        }
    }

    fun sendSignal(toPeerId: String, data: JSONObject) {
        val msg = JSONObject()
            .put("type", "signal")
            .put("to", toPeerId)
            .put("data", data)
        socket?.send(msg.toString())
    }

    fun disconnect() {
        socket?.close(1000, "bye")
        socket = null
    }
}
