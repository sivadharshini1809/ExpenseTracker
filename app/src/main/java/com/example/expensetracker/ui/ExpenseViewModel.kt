package com.example.expensetracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.expensetracker.data.AppDatabase
import com.example.expensetracker.data.Expense
import com.example.expensetracker.util.NotificationHelper
import com.example.expensetracker.util.Prefs
import com.example.expensetracker.util.monthRange

class ExpenseViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).expenseDao()
    val expenses = dao.getAll()

    suspend fun insert(e: Expense) {
        dao.insert(e)
        checkBudget()
    }

    suspend fun update(e: Expense) {
        dao.update(e)
        checkBudget()
    }

    suspend fun delete(e: Expense) = dao.delete(e)

    suspend fun getById(id: Int) = dao.getById(id)

    private suspend fun checkBudget() {
        val ctx = getApplication<Application>()
        val budget = Prefs.getBudget(ctx)
        if (budget <= 0f) return
        val (s, e) = monthRange()
        val total = dao.totalBetween(s, e)
        if (total >= budget * 0.8) NotificationHelper.showBudgetAlert(ctx, total, budget)
    }
}
