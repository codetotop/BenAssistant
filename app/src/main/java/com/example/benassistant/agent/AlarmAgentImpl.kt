package com.example.benassistant.agent

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock

class AlarmAgentImpl(
    private val context: Context
) : AlarmAgent {

    override fun setAlarm(hour: Int, minute: Int, label: String?) {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_MESSAGE, label)
            putExtra(AlarmClock.EXTRA_SKIP_UI, true) // Bỏ qua UI của ứng dụng đồng hồ
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun openClockApp() {
        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
