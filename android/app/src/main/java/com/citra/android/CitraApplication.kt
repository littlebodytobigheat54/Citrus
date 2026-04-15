package com.citra.android

import android.app.Application
import android.content.Context
import com.citra.android.utils.PreferenceManager

class CitraApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        PreferenceManager.init(this)
    }

    companion object {
        lateinit var instance: CitraApplication
            private set

        val context: Context get() = instance.applicationContext
    }
}
