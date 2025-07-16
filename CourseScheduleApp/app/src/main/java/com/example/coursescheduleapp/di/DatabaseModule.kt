package com.example.coursescheduleapp.di

import android.content.Context
import androidx.room.Room
import com.example.coursescheduleapp.db.AppDatabase
import com.example.coursescheduleapp.db.SectionSettingDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        )
            .fallbackToDestructiveMigration()// 允许无迁移时直接重建数据库
            .build()
    }

    @Provides
    fun provideSectionSettingDao(appDatabase: AppDatabase): SectionSettingDao =
        appDatabase.sectionSettingDao()
} 