package com.intentionalphone.app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private val purposes = listOf("Call", "Text", "Social media", "Shopping", "Searching", "Chess", "Reading", "Navigation", "Work", "Other")
    private var selectedPurpose: String? = null
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()
        render()
    }

    private fun render() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(28), dp(24), dp(24))
            setBackgroundColor(Color.rgb(246, 247, 244))
        }
        root.addView(TextView(this).apply {
            text = "Intentional Phone"
            textSize = 28f
            setTextColor(Color.rgb(29, 48, 38))
            setTypeface(typeface, Typeface.BOLD)
        })
        root.addView(TextView(this).apply {
            text = "A gentle reminder of why you picked up your phone. No blocking. No judgement."
            textSize = 16f
            setTextColor(Color.rgb(83, 91, 85))
            setPadding(0, dp(8), 0, dp(22))
        })
        root.addView(TextView(this).apply {
            text = "WHAT'S YOUR PURPOSE?"
            textSize = 12f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.rgb(75, 91, 79))
            setPadding(0, 0, 0, dp(10))
        })
        purposes.chunked(2).forEach { pair ->
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            pair.forEach { purpose ->
                val button = Button(this).apply {
                    text = purpose
                    isAllCaps = false
                    setOnClickListener {
                        if (!Settings.canDrawOverlays(this@MainActivity)) {
                            selectedPurpose = purpose
                            Toast.makeText(this@MainActivity, "Allow display over other apps to show the reminder.", Toast.LENGTH_LONG).show()
                            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                        } else {
                            selectedPurpose = null
                            startReminder(purpose)
                        }
                    }
                }
                row.addView(button, LinearLayout.LayoutParams(0, dp(54), 1f).apply {
                    setMargins(dp(3), dp(3), dp(3), dp(3))
                })
            }
            root.addView(row)
        }
        val skip = Button(this).apply {
            text = "Skip for now"
            isAllCaps = false
            setOnClickListener {
                stopService(Intent(this@MainActivity, ReminderOverlayService::class.java))
                Toast.makeText(this@MainActivity, "Skipped. Your phone is unchanged.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
        root.addView(skip, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)).apply { topMargin = dp(12) })

        status = TextView(this).apply {
            text = "Overlay permission is needed only to show the floating reminder. You can dismiss it at any time."
            textSize = 13f
            setTextColor(Color.rgb(93, 100, 94))
            setPadding(0, dp(16), 0, 0)
        }
        root.addView(status)
        root.addView(Button(this).apply {
            text = "Stop reminder"
            isAllCaps = false
            setOnClickListener {
                stopService(Intent(this@MainActivity, ReminderOverlayService::class.java))
                Toast.makeText(this@MainActivity, "Reminder stopped", Toast.LENGTH_SHORT).show()
            }
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48)).apply { topMargin = dp(8) })
        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        if (::status.isInitialized && selectedPurpose != null && Settings.canDrawOverlays(this)) {
            val purpose = selectedPurpose!!
            selectedPurpose = null
            startReminder(purpose)
        }
    }

    private fun startReminder(purpose: String) {
        if (!Settings.canDrawOverlays(this)) return
        val intent = Intent(this, ReminderOverlayService::class.java).putExtra(ReminderOverlayService.EXTRA_PURPOSE, purpose)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent) else startService(intent)
        Toast.makeText(this, "Reminder started. Close it whenever you like.", Toast.LENGTH_SHORT).show()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 41)
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
