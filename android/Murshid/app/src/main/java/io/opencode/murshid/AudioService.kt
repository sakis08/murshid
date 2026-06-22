package io.opencode.murshid

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Base64
import androidx.core.app.NotificationCompat
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class AudioService : Service() {
    private var ws: WebSocketClient? = null
    private var audioTrack: AudioTrack? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var buffer = byteArrayOf()

    companion object {
        var isRunning = false
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        createNotificationChannel()
        startForeground(1, buildNotification("جارٍ الاتصال..."))

        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "murshid:audio")
        wakeLock?.acquire(10 * 60 * 1000L)

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build())
            .setAudioFormat(AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(48000)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build())
            .setBufferSizeInBytes(AudioTrack.getMinBufferSize(48000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT) * 4)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val ip = intent?.getStringExtra("server_ip") ?: return START_NOT_STICKY
        val stream = intent?.getStringExtra("stream_name") ?: return START_NOT_STICKY

        connect(ip, stream)
        return START_STICKY
    }

    private fun connect(ip: String, stream: String) {
        val uri = URI("ws://$ip:6789?stream=${java.net.URLEncoder.encode(stream, "UTF-8")}&role=controller")
        ws = object : WebSocketClient(uri) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                updateNotification("🟢 متصل — $stream")
                audioTrack?.play()
            }

            override fun onMessage(message: String) {
                try {
                    val json = org.json.JSONObject(message)
                    if (json.getString("type") == "audio") {
                        val raw = json.getString("data")
                        val bytes = Base64.decode(raw, Base64.DEFAULT)
                        audioTrack?.write(bytes, 0, bytes.size)
                    }
                } catch (_: Exception) {}
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                updateNotification("🔴 قطع الاتصال")
                audioTrack?.pause()
                audioTrack?.flush()
                // إعادة محاولة الاتصال بعد 5 ثواني
                android.os.Handler(mainLooper).postDelayed({ connect(ip, stream) }, 5000)
            }

            override fun onError(ex: Exception?) {
                updateNotification("🔴 خطأ في الاتصال")
            }
        }
        ws?.connect()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "murshid_audio",
                "مرشد - البث الصوتي",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "إشعار اتصال البث الصوتي من غرفة العمليات"
                setSound(null, null)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String): Notification {
        return NotificationCompat.Builder(this, "murshid_audio")
            .setContentTitle("🎙 مرشد")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(text: String) {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(1, buildNotification(text))
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        isRunning = false
        ws?.close()
        audioTrack?.stop()
        audioTrack?.release()
        wakeLock?.release()
        super.onDestroy()
    }
}
