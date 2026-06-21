package io.opencode.murshid

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat

class MainActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences
    private lateinit var serverIp: EditText
    private lateinit var streamName: EditText
    private lateinit var statusText: TextView
    private lateinit var connectBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("murshid", MODE_PRIVATE)
        serverIp = findViewById(R.id.serverIp)
        streamName = findViewById(R.id.streamName)
        statusText = findViewById(R.id.statusText)
        connectBtn = findViewById(R.id.connectBtn)

        serverIp.setText(prefs.getString("server_ip", ""))
        streamName.setText(prefs.getString("stream_name", ""))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            NotificationManagerCompat.from(this).areNotificationsEnabled()
        }

        connectBtn.setOnClickListener {
            val ip = serverIp.text.toString().trim()
            val name = streamName.text.toString().trim()
            if (ip.isEmpty() || name.isEmpty()) return@setOnClickListener

            prefs.edit().putString("server_ip", ip).putString("stream_name", name).apply()

            val intent = Intent(this, AudioService::class.java).apply {
                putExtra("server_ip", ip)
                putExtra("stream_name", name)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }

            statusText.text = "🟢 متصل"
            statusText.setTextColor(0xFF22c55e.toInt())
            connectBtn.text = "🔴 قطع الاتصال"
        }
    }

    override fun onResume() {
        super.onResume()
        val isRunning = AudioService.isRunning
        if (isRunning) {
            statusText.text = "🟢 متصل"
            statusText.setTextColor(0xFF22c55e.toInt())
            connectBtn.text = "🔴 قطع الاتصال"
        }
    }
}
