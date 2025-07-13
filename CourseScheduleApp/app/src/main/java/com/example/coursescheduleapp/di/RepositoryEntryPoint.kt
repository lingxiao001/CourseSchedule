package com.example.coursescheduleapp.di

import com.example.coursescheduleapp.repository.UserAdminRepository
import com.example.coursescheduleapp.repository.CourseAdminRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface RepositoryEntryPoint {
    fun userAdminRepository(): UserAdminRepository
    fun courseAdminRepository(): CourseAdminRepository
} 