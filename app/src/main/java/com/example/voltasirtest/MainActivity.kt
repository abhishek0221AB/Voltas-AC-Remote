package com.example.voltasirtest

import android.content.Context
import android.hardware.ConsumerIrManager
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private lateinit var irManager: ConsumerIrManager
    private lateinit var webView: WebView
    private val encoder = VoltasEncoder()

    companion object {
        // Keep blank for the first bridge test. Later put your GitHub Pages URL here.
        private const val REMOTE_URL = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        irManager = getSystemService(Context.CONSUMER_IR_SERVICE) as ConsumerIrManager
        webView = findViewById(R.id.remoteWebView)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = WebViewClient()
        webView.addJavascriptInterface(IRBridge(this), "AndroidIR")

        if (REMOTE_URL.isBlank()) {
            webView.loadUrl("file:///android_asset/web/index.html")
        } else {
            webView.loadUrl(REMOTE_URL)
        }
    }

    fun handleWebMessage(message: JSONObject) {
        if (!irManager.hasIrEmitter()) {
            reportBridgeError("No IR emitter available")
            return
        }

        val action = message.optString("action")
        val state = message.optJSONObject("state")

        val power = state?.optBoolean("power") ?: false
        val temperature = state?.optInt("temperature") ?: 24
        val mode = state?.optString("mode") ?: "COOL"
        val fan = state?.optString("fan") ?: "AUTO"
        val swing = state?.optBoolean("verticalSwing") ?: false
        val lamp = state?.optBoolean("lamp") ?: true
        val turbo = state?.optBoolean("turbo") ?: false

        val pattern: IntArray
        val label: String

        when (action) {
            "power" -> {
                pattern = if (power) encoder.powerOn() else encoder.powerOff()
                label = if (power) "POWER ON" else "POWER OFF"
            }
            "temp-up", "temp-down" -> {
                pattern = encoder.setTemperatureCommand(temperature)
                label = "TEMP ${temperature}C"
            }
            "mode" -> when (mode) {
                "DRY" -> { pattern = encoder.modeDry(); label = "MODE DRY" }
                "FAN" -> { pattern = encoder.modeFan(); label = "MODE FAN" }
                else -> { pattern = encoder.modeCool(); label = "MODE COOL" }
            }
            "fan" -> when (fan) {
                "LOW" -> { pattern = encoder.fanLow(); label = "FAN LOW" }
                "MEDIUM" -> { pattern = encoder.fanMedium(); label = "FAN MEDIUM" }
                "HIGH" -> { pattern = encoder.fanHigh(); label = "FAN HIGH" }
                else -> { pattern = encoder.fanAuto(); label = "FAN AUTO" }
            }
            "v-swing" -> {
                pattern = if (swing) encoder.verticalSwingOn() else encoder.verticalSwingOff()
                label = if (swing) "V-SWING ON" else "V-SWING OFF"
            }
            "lamp" -> {
                pattern = if (lamp) encoder.lampOn() else encoder.lampOff()
                label = if (lamp) "LAMP ON" else "LAMP OFF"
            }
            "turbo" -> {
                pattern = if (turbo) encoder.turboOn() else encoder.turboOff()
                label = if (turbo) "TURBO ON" else "TURBO OFF"
            }
            "timer-set" -> {
                val hours = message.optInt("hours", 1).coerceIn(1, 15)
                val timerType = message.optString("timerType", "on")
                if (timerType == "off") {
                    pattern = encoder.timerOffHours(hours)
                    label = "TIMER OFF ${hours}H"
                } else {
                    pattern = encoder.timerOnHours(hours)
                    label = "TIMER ON ${hours}H"
                }
            }
            "timer-cancel" -> {
                val timerType = message.optString("timerType", "on")
                if (timerType == "off") {
                    pattern = encoder.cancelOffTimer()
                    label = "CANCEL TIMER OFF"
                } else {
                    pattern = encoder.cancelOnTimer()
                    label = "CANCEL TIMER ON"
                }
            }
            else -> {
                reportBridgeError("Unknown action: $action")
                return
            }
        }

        transmit(label, pattern)
    }

    private fun transmit(label: String, pattern: IntArray) {
        try {
            irManager.transmit(VoltasEncoder.CARRIER_HZ, pattern)
            sendStatus("Sent: $label | ${encoder.rawStateHex()}")
        } catch (e: Exception) {
            reportBridgeError(e.message ?: "Transmit failed")
        }
    }

    fun reportBridgeError(message: String) = sendStatus("Error: $message")

    private fun sendStatus(message: String) {
        val safe = JSONObject.quote(message)
        webView.evaluateJavascript("window.updateBridgeStatus?.($safe);", null)
    }
}
