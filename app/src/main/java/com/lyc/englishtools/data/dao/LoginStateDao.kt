package com.lyc.englishtools.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lyc.englishtools.data.LoginState

@Dao
interface LoginStateDao {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(state: LoginState)

    @Update
    suspend fun update(state: LoginState)

    @Query("SELECT * FROM login_state WHERE userId = 1")
    suspend fun getState(): LoginState?

    @Query("UPDATE login_state SET isLoggedIn = :isLoggedIn, username = :username WHERE userId = 1")
    suspend fun updateState(isLoggedIn: Boolean, username: String?)

    @Query("DELETE FROM login_state")
    suspend fun clearState()
}