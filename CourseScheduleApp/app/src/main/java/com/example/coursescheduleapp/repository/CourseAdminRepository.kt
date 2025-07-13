package com.example.coursescheduleapp.repository

import com.example.coursescheduleapp.model.Course
import retrofit2.http.*

interface CourseAdminRepository {
    
    @GET("api/courses")
    suspend fun getAllCourses(): List<Course>
    
    @GET("api/courses/{id}")
    suspend fun getCourseById(@Path("id") id: Long): Course
    
    @POST("api/courses")
    suspend fun createCourse(@Body course: CourseCreateRequest): Course
    
    @PUT("api/courses/{id}")
    suspend fun updateCourse(@Path("id") id: Long, @Body course: CourseUpdateRequest): Course
    
    @DELETE("api/courses/{id}")
    suspend fun deleteCourse(@Path("id") id: Long)
} 