package com.example.voltasirtest

import android.webkit.JavascriptInterface
import org.json.JSONObject

class IRBridge(private val activity: MainActivity) {
    @JavascriptInterface
    fun sendState(json: String) {
        try {
            val obj = JSONObject(json)
            activity.runOnUiThread { activity.handleWebMessage(obj) }
        } catch (e: Exception) {
            activity.runOnUiThread { activity.reportBridgeError(e.message ?: "Bridge error") }
        }
    }
}
