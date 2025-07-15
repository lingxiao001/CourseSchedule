package com.example.coursescheduleapp

import android.app.Application
import androidx.room.Room
import com.example.coursescheduleapp.db.AppDatabase
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CourseScheduleApplication : Application() {
    lateinit var db: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(
            this,
            AppDatabase::class.java, "app_database"
        ).build()
    }
} 