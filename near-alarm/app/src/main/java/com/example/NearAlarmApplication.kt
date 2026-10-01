package com.example

import android.app.Application
import android.preference.PreferenceManager
import com.example.data.db.AppDatabase
import com.example.data.pref.AppSettings
import com.example.data.repository.AlarmRepository
import com.example.geofence.GeofenceManager
import com.example.geofence.NotificationHelper
import com.example.map.MapConfig
import org.osmdroid.config.Configuration

class NearAlarmApplication : Application() {
    lateinit var database: AppDatabase
        private set
    lateinit var settings: AppSettings
        private set
    lateinit var repository: AlarmRepository
        private set
    lateinit var geofenceManager: GeofenceManager
        private set

    override fun onCreate() {
        super.onCreate()

        // 1. Initialize osmdroid configuration for caching tiles politely
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        Configuration.getInstance().userAgentValue = MapConfig.DEFAULT_USER_AGENT

        // 2. Initialize notification channels
        NotificationHelper.createNotificationChannels(this)

        // 3. Initialize Database, Settings, and Repository
        database = AppDatabase.getInstance(this)
        settings = AppSettings(this)
        repository = AlarmRepository(database.alarmDao(), database.historyDao(), settings)
        geofenceManager = GeofenceManager(this)
    }
}
