package com.dascon.machinerylog

import android.app.Application
import com.dascon.machinerylog.data.repo.AppContainer

class DasconLogApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
