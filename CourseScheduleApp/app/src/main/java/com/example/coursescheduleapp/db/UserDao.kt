package com.example.coursescheduleapp.db

import androidx.room.*

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    suspend fun getAll(): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<UserEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Update
    suspend fun update(user: UserEntity)

    @Query("DELETE FROM user WHERE userId = :userId")
    suspend fun deleteById(userId: Long)

    @Query("DELETE FROM user")
    suspend fun deleteAll()
} 