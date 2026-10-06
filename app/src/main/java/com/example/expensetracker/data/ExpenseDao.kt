package com.example.expensetracker.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ExpenseDao {
    @Insert suspend fun insert(e: Expense)
    @Update suspend fun update(e: Expense)
    @Delete suspend fun delete(e: Expense)

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAll(): LiveData<List<Expense>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getById(id: Int): Expense?

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE date BETWEEN :start AND :end")
    suspend fun totalBetween(start: Long, end: Long): Double
}
