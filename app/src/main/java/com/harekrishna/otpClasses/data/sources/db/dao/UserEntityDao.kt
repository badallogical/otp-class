package com.harekrishna.otpClasses.data.sources.db.dao

import androidx.room.*
import com.harekrishna.otpClasses.data.models.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Update
    suspend fun update(user: UserEntity)

    @Delete
    suspend fun delete(user: UserEntity)

    @Query("SELECT COUNT(*) FROM `user`")
    suspend fun tableSize() : Int

    @Query("DELETE FROM `user` WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM `user`")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM `user` WHERE id = :id LIMIT 1")
    fun getUser(id: String): Flow<UserEntity?>

    @Query("SELECT name FROM `user` WHERE id = :id LIMIT 1")
    fun getUserName(id: String): Flow<String?>

    @Query("SELECT phone FROM `user` WHERE id = :id LIMIT 1")
    fun getUserPhone(id: String): Flow<String?>

    @Query("SELECT * FROM `user` WHERE id = :id LIMIT 1")
    fun getUserById(id: String): Flow<UserEntity>

    @Query("SELECT * FROM `user` WHERE id = :id LIMIT 1")
    fun observeCurrentUser(id: String): Flow<UserEntity>

    @Query("UPDATE `user` SET name = :name WHERE id = :id")
    suspend fun updateName(id: String, name: String)

    @Query("UPDATE `user` SET phone = :phone WHERE id = :id")
    suspend fun updatePhone(id: String, phone: String)

    @Query("UPDATE `user` SET photoURL = :photoURL WHERE id = :id")
    suspend fun updatePhotoUrl(id: String, photoURL: String)

    @Query("UPDATE `user` SET email = :email WHERE id = :id")
    suspend fun updateEmail(id: String, email: String)

    @Query("UPDATE `user` SET isGuest = :isGuest WHERE id = :id")
    suspend fun updateGuestStatus(id: String, isGuest: Boolean)

    @Query("UPDATE `user` SET role = :role WHERE id = :id")
    suspend fun updateRole(id: String, role: String)

    @Query("UPDATE `user` SET name = :name, phone = :phone WHERE id = :id")
    suspend fun updateProfile(id: String, name: String, phone: String)
}
