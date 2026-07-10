package com.tvcostabrava.intercom

import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.webrtc.PeerConnection
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Pide credenciales TURN reales (temporales) a traves de nuestro propio servidor de
 * senializacion (server/index.js expone /turn-credentials), que a su vez las pide a
 * metered.ca. La API key de metered.ca nunca viaja al APK, solo vive en el servidor.
 */
object TurnCredentialsFetcher {
    private const val TAG = "TurnCredentialsFetcher"

    private val client = OkHttpClient.Builder()
        .callTimeout(4, TimeUnit.SECONDS)
        .build()

    /** Llama a onResult exactamente una vez; lista vacia si falla, no hay red, o timeout. */
    fun fetch(turnCredentialsUrl: String, onResult: (List<PeerConnection.IceServer>) -> Unit) {
        val request = try {
            Request.Builder().url(turnCredentialsUrl).build()
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "URL invalida: $turnCredentialsUrl", e)
            onResult(emptyList())
            return
        }

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.w(TAG, "No se pudieron obtener credenciales TURN: ${e.message}")
                onResult(emptyList())
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    try {
                        val body = it.body?.string().orEmpty()
                        if (!it.isSuccessful) {
                            Log.w(TAG, "turn-credentials HTTP ${it.code}: $body")
                            onResult(emptyList())
                            return
                        }
                        val arr = JSONArray(body)
                        val servers = (0 until arr.length()).mapNotNull { i ->
                            val obj = arr.getJSONObject(i)
                            val urls = obj.optString("urls")
                            if (urls.isBlank()) return@mapNotNull null
                            val builder = PeerConnection.IceServer.builder(urls)
                            val username = obj.optString("username")
                            val credential = obj.optString("credential")
                            if (username.isNotBlank()) builder.setUsername(username)
                            if (credential.isNotBlank()) builder.setPassword(credential)
                            builder.createIceServer()
                        }
                        Log.i(TAG, "credenciales TURN recibidas: ${servers.size}")
                        onResult(servers)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parseando credenciales TURN", e)
                        onResult(emptyList())
                    }
                }
            }
        })
    }
}
