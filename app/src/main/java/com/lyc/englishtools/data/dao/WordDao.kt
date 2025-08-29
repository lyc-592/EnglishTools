package com.lyc.englishtools.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lyc.englishtools.data.entities.Bookmark
import com.lyc.englishtools.data.entities.WordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(words: List<WordEntity>)

    @Query("SELECT * FROM words")
    suspend fun getAllWords(): List<WordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: Bookmark)

    @Query("SELECT * FROM bookmarks WHERE userId = :userId")
    suspend fun getBookmark(userId: String): Bookmark?

    @Query("SELECT * FROM words WHERE id IN (:ids)")
    suspend fun getWordsByIds(ids: List<Int>): List<WordEntity>

    @Query("DELETE FROM words")
    suspend fun deleteAllWords()

    @Query("DELETE FROM bookmarks")
    suspend fun deleteAllBookmarks()
}