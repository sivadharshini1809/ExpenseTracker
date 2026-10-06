package com.example.expensetracker.ui

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.expensetracker.data.Expense
import com.example.expensetracker.databinding.ActivityAddExpenseBinding
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var b: ActivityAddExpenseBinding
    private val vm: ExpenseViewModel by viewModels()
    private val categories = listOf("Food", "Travel", "Shopping", "Bills", "Other")
    private var editId = 0
    private var dateMillis = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.spCategory.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)

        editId = intent.getIntExtra("id", 0)
        if (editId != 0) {
            title = "Edit expense"
            lifecycleScope.launch {
                vm.getById(editId)?.let { e ->
                    b.etAmount.setText(e.amount.toString())
                    b.etMerchant.setText(e.merchant)
                    b.etNote.setText(e.note)
                    b.spCategory.setSelection(categories.indexOf(e.category).coerceAtLeast(0))
                    dateMillis = e.date
                    updateDateText()
                }
            }
        } else {
            // Values coming from bill scan (if any)
            val amount = intent.getDoubleExtra("amount", 0.0)
            if (amount > 0) b.etAmount.setText(amount.toString())
            intent.getStringExtra("merchant")?.let { b.etMerchant.setText(it) }
            dateMillis = intent.getLongExtra("date", dateMillis)
        }
        updateDateText()

        b.btnDate.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker().setSelection(dateMillis).build()
            picker.addOnPositiveButtonClickListener { dateMillis = it; updateDateText() }
            picker.show(supportFragmentManager, "date")
        }
        b.btnSave.setOnClickListener { save() }
    }

    private fun updateDateText() {
        b.btnDate.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(dateMillis)
    }

    private fun save() {
        val amount = b.etAmount.text.toString().toDoubleOrNull()
        if (amount == null || amount <= 0) {
            b.etAmount.error = "Enter a valid amount"
            return
        }
        val e = Expense(
            id = editId,
            amount = amount,
            category = b.spCategory.selectedItem.toString(),
            merchant = b.etMerchant.text.toString().trim(),
            date = dateMillis,
            note = b.etNote.text.toString().trim()
        )
        lifecycleScope.launch {
            if (editId == 0) vm.insert(e) else vm.update(e)
            finish()
        }
    }
}
