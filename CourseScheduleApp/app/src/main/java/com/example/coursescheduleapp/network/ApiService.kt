package com.example.coursescheduleapp.network

import com.example.coursescheduleapp.model.*
import com.example.coursescheduleapp.repository.UserListResponse
import com.example.coursescheduleapp.repository.UserCreateRequest
import com.example.coursescheduleapp.repository.UserUpdateRequest
import retrofit2.Response
import retrofit2.http.*
import okhttp3.ResponseBody

interface ApiService {
    
    // 认证相关
    @POST("api/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<ResponseBody>
    
    @POST("api/auth/register")
    suspend fun register(@Body userCreateRequest: UserCreateRequest): Response<ResponseBody>
    
    @GET("api/auth/me")
    suspend fun getCurrentUser(): Response<ResponseBody>
    
    // 课程相关
    @GET("api/courses")
    suspend fun getAllCourses(): Response<List<Course>>
    
    @GET("api/courses/{id}")
    suspend fun getCourseById(@Path("id") id: Long): Response<Course>
    
    @POST("api/courses")
    suspend fun createCourse(@Body course: Course): Response<Course>
    
    @PUT("api/courses/{id}")
    suspend fun updateCourse(@Path("id") id: Long, @Body course: Course): Response<Course>
    
    @DELETE("api/courses/{id}")
    suspend fun deleteCourse(@Path("id") id: Long): Response<Void>
    
    // 教学班相关
    @GET("api/courses/classes")
    suspend fun getAllTeachingClasses(): Response<List<TeachingClass>>
    
    @GET("api/courses/{courseId}/classes")
    suspend fun getTeachingClassesByCourse(@Path("courseId") courseId: Long): Response<List<TeachingClass>>
    
    @POST("api/courses/{courseId}/classes")
    suspend fun createTeachingClass(
        @Path("courseId") courseId: Long,
        @Body teachingClass: TeachingClass
    ): Response<TeachingClass>
    
    @PUT("api/courses/classes/{classId}")
    suspend fun updateTeachingClass(
        @Path("classId") classId: Long,
        @Body teachingClass: TeachingClass
    ): Response<TeachingClass>
    
    @DELETE("api/courses/classes/{classId}")
    suspend fun deleteTeachingClass(@Path("classId") classId: Long): Response<Void>
    
    // 选课相关
    @GET("api/selections/dto")
    suspend fun getAllSelectionDTOs(): Response<List<SelectionDTO>>
    
    @POST("api/selections")
    suspend fun selectCourse(
        @Query("studentId") studentId: Long,
        @Query("teachingClassId") teachingClassId: Long
    ): Response<String>
    
    @DELETE("api/selections")
    suspend fun cancelSelection(
        @Query("studentId") studentId: Long,
        @Query("teachingClassId") teachingClassId: Long
    ): Response<String>
    
    @GET("api/selections/student/{studentId}")
    suspend fun getSelectionsByStudent(@Path("studentId") studentId: Long): Response<List<SelectionDTO>>
    
    @GET("api/selections/my-courses/student/{studentId}")
    suspend fun getMyCourses(@Path("studentId") studentId: Long): Response<List<MyCourseDTO>>
    
    @GET("api/selections/available-courses/student/{studentId}")
    suspend fun getAvailableCourses(@Path("studentId") studentId: Long): Response<List<AvailableCourseDTO>>
    
    @GET("api/selections/available-courses-by-course/student/{studentId}")
    suspend fun getAvailableCoursesGroupedByCourse(@Path("studentId") studentId: Long): Response<List<CourseWithTeachingClassesDTO>>
    
    // 课表相关
    @GET("api/schedules")
    suspend fun getAllSchedules(): Response<List<ClassSchedule>>
    
    @GET("api/schedules/teaching-class/{teachingClassId}")
    suspend fun getSchedulesByTeachingClass(@Path("teachingClassId") teachingClassId: Long): Response<List<ClassSchedule>>
    
    @GET("api/schedules/teacher/{teacherId}")
    suspend fun getSchedulesByTeacher(@Path("teacherId") teacherId: Long): Response<List<ClassSchedule>>
    
    // 教室相关
    @GET("api/classrooms")
    suspend fun getAllClassrooms(): Response<List<Classroom>>
    
    @GET("api/classrooms/{classroomId}")
    suspend fun getClassroomById(@Path("classroomId") classroomId: Long): Response<Classroom>
    
    @POST("api/classrooms")
    suspend fun createClassroom(@Body classroom: Classroom): Response<Classroom>

    @PUT("api/classrooms/{classroomId}")
    suspend fun updateClassroom(@Path("classroomId") classroomId: Long, @Body classroom: Classroom): Response<Classroom>

    @DELETE("api/classrooms/{classroomId}")
    suspend fun deleteClassroom(@Path("classroomId") classroomId: Long): Response<Void>
    
    // 用户管理（管理员）- 更新为支持分页和搜索
    @GET("api/admin/users")
    suspend fun getUsers(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("search") search: String? = null
    ): Response<UserListResponse>
    
    @POST("api/admin/users")
    suspend fun createUser(@Body user: UserCreateRequest): Response<User>
    
    @GET("api/admin/users/{userId}")
    suspend fun getUser(@Path("userId") userId: Long): Response<User>
    
    @PUT("api/admin/users/{userId}")
    suspend fun updateUser(@Path("userId") userId: Long, @Body user: UserUpdateRequest): Response<User>
    
    @DELETE("api/admin/users/{userId}")
    suspend fun deleteUser(@Path("userId") userId: Long): Response<Void>
    
    // 统计信息
    @GET("api/admin/stats")
    suspend fun getStats(): Response<Map<String, Long>>

    // 手动排课相关
    @POST("api/schedules/teaching-class/{teachingClassId}")
    suspend fun addSchedule(
        @Path("teachingClassId") teachingClassId: Long,
        @Body scheduleData: Map<String, @JvmSuppressWildcards Any>
    ): Response<Any>

    @PUT("api/schedules/{scheduleId}")
    suspend fun updateSchedule(
        @Path("scheduleId") scheduleId: Long,
        @Body scheduleData: Map<String, @JvmSuppressWildcards Any>
    ): Response<Any>

    @DELETE("api/schedules/{scheduleId}")
    suspend fun deleteSchedule(
        @Path("scheduleId") scheduleId: Long
    ): Response<Any>

    // 批量删除全校排课
    @DELETE("api/schedules/all")
    suspend fun deleteAllSchedules(): Response<Void>

    // 智能排课（单个教学班）
    @POST("api/intelligent-scheduling/auto-schedule/{teachingClassId}")
    suspend fun autoScheduleForTeachingClass(@Path("teachingClassId") teachingClassId: Long): Response<List<ClassSchedule>>

    // 智能排课（全部教学班）
    @POST("api/intelligent-scheduling/auto-schedule")
    suspend fun autoScheduleForAllTeachingClasses(): Response<List<ClassSchedule>>

    // 重置密码
    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body req: ResetPasswordDTO): retrofit2.Response<Void>
} 