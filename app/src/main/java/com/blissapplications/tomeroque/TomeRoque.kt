package com.blissapplications.tomeroque

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TomeRoque : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}