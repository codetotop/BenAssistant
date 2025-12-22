package com.example.benassistant.agent

import android.content.Context
import android.content.Intent
import android.net.Uri

class MapAgentImpl(
    private val context: Context
) : MapAgent {

    override fun openMap(destination: String) {
        val uri = Uri.parse("geo:0,0?q=${Uri.encode(destination)}")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
        context.startActivity(intent)
    }
}

