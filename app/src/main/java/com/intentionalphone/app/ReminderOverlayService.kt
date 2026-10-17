package com.intentionalphone.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView

class ReminderOverlayService : Service() {
    private var windowManager: WindowManager? = null
    private var overlay: View? = null
    private var params: WindowManager.LayoutParams? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val purpose = intent?.getStringExtra(EXTRA_PURPOSE) ?: "Your purpose"
        showOverlay(purpose)
        return START_NOT_STICKY
    }

    private fun showOverlay(purpose: String) {
        removeOverlay()
        val density = resources.displayMetrics.density
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding((14 * density).toInt(), (8 * density).toInt(), (8 * density).toInt(), (8 * density).toInt())
            setBackgroundColor(Color.rgb(32, 45, 37))
            elevation = 12 * density
        }
        val label = TextView(this).apply {
            text = "●  Purpose: $purpose"
            textSize = 14f
            setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
            maxLines = 2
        }
        panel.addView(label, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        val close = TextView(this).apply {
            text = "✕"
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setPadding((12 * density).toInt(), (5 * density).toInt(), (5 * density).toInt(), (5 * density).toInt())
            contentDescription = "Dismiss purpose reminder"
            setOnClickListener { stopSelf() }
        }
        panel.addView(close)
        val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE
        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            windowType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = (70 * density).toInt()
        }
        // Drag the reminder out of the way without intercepting touches to the app beneath it.
        label.setOnTouchListener(object : View.OnTouchListener {
            var startX = 0
            var startY = 0
            var downX = 0f
            var downY = 0f
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = lp.x; startY = lp.y
                        downX = event.rawX; downY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        lp.x = startX + (event.rawX - downX).toInt()
                        lp.y = startY + (event.rawY - downY).toInt()
                        try { windowManager?.updateViewLayout(panel, lp) } catch (_: Exception) { }
                        return true
                    }
                }
                return false
            }
        })
        try {
            windowManager?.addView(panel, lp)
            overlay = panel
            params = lp
        } catch (_: Exception) {
            stopSelf()
        }
    }

    private fun removeOverlay() {
        overlay?.let { view -> try { windowManager?.removeView(view) } catch (_: Exception) { } }
        overlay = null
        params = null
    }

    override fun onDestroy() {
        removeOverlay()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Purpose reminder", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Keeps your manually started purpose reminder running"
            }
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val openApp = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0
        )
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Intentional Phone is active")
                .setContentText("Your floating reminder is dismissible. Tap to manage it.")
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .setContentIntent(openApp)
                .setOngoing(true)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("Intentional Phone is active")
                .setContentText("Your floating reminder is dismissible.")
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .setContentIntent(openApp)
                .setOngoing(true)
                .build()
        }
    }

    companion object {
        const val EXTRA_PURPOSE = "purpose"
        private const val CHANNEL_ID = "purpose_reminder"
        private const val NOTIFICATION_ID = 1001
    }
}
