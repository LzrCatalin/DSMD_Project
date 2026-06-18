package com.example.readingcorner.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ShelfBookDao {

    @Query("SELECT * FROM shelf_books WHERE userUid = :userUid ORDER BY addedAt DESC")
    fun observeAll(userUid: String): Flow<List<ShelfBook>>

    @Query("SELECT * FROM shelf_books WHERE userUid = :userUid AND status = :status ORDER BY addedAt DESC")
    fun observeByStatus(userUid: String, status: ShelfStatus): Flow<List<ShelfBook>>

    @Query("SELECT * FROM shelf_books WHERE userUid = :userUid AND googleId = :googleId LIMIT 1")
    fun observeByGoogleId(userUid: String, googleId: String): Flow<ShelfBook?>

    @Query("SELECT * FROM shelf_books WHERE userUid = :userUid AND googleId = :googleId LIMIT 1")
    suspend fun getByGoogleId(userUid: String, googleId: String): ShelfBook?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(book: ShelfBook)

    @Query("UPDATE shelf_books SET status = :status WHERE userUid = :userUid AND googleId = :googleId")
    suspend fun updateStatus(userUid: String, googleId: String, status: ShelfStatus)

    @Query("UPDATE shelf_books SET myRating = :rating WHERE userUid = :userUid AND googleId = :googleId")
    suspend fun updateRating(userUid: String, googleId: String, rating: Float)

    @Query("DELETE FROM shelf_books WHERE userUid = :userUid AND googleId = :googleId")
    suspend fun delete(userUid: String, googleId: String)

    @Query("SELECT COUNT(*) FROM shelf_books WHERE userUid = :userUid AND status = :status")
    fun countByStatus(userUid: String, status: ShelfStatus): Flow<Int>
}
