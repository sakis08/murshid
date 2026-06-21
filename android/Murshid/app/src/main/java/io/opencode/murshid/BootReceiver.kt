package io.opencode.murshid

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val prefs = context.getSharedPreferences("murshid", Context.MODE_PRIVATE)
        val ip = prefs.getString("server_ip", "")
        val stream = prefs.getString("stream_name", "")

        if (ip.isNullOrEmpty() || stream.isNullOrEmpty()) return

        val serviceIntent = Intent(context, AudioService::class.java).apply {
            putExtra("server_ip", ip)
            putExtra("stream_name", stream)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
