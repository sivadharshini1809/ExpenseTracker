package com.example.expensetracker.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.data.Expense
import com.example.expensetracker.databinding.ActivityMainBinding
import com.example.expensetracker.util.Prefs
import com.example.expensetracker.util.monthRange
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private val vm: ExpenseViewModel by viewModels()
    private val adapter = ExpenseAdapter { e ->
        startActivity(Intent(this, AddExpenseActivity::class.java).putExtra("id", e.id))
    }
    private var all: List<Expense> = emptyList()
    private var filter = "All"

    private val notifPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)

        b.recycler.layoutManager = LinearLayoutManager(this)
        b.recycler.adapter = adapter

        vm.expenses.observe(this) { all = it; refresh() }

        b.chipGroup.setOnCheckedStateChangeListener { group, ids ->
            filter = if (ids.isEmpty()) "All"
            else group.findViewById<Chip>(ids.first()).text.toString()
            refresh()
        }

        val swipe = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                r: RecyclerView, v: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int) {
                val e = adapter.currentList[vh.bindingAdapterPosition]
                lifecycleScope.launch { vm.delete(e) }
                Snackbar.make(b.root, "Expense deleted", Snackbar.LENGTH_LONG)
                    .setAction("Undo") { lifecycleScope.launch { vm.insert(e) } }
                    .show()
            }
        }
        ItemTouchHelper(swipe).attachToRecyclerView(b.recycler)

        b.tvBudget.setOnClickListener { showBudgetDialog() }
        b.btnAdd.setOnClickListener { startActivity(Intent(this, AddExpenseActivity::class.java)) }
        b.btnScan.setOnClickListener { startActivity(Intent(this, ScanActivity::class.java)) }
        b.btnReport.setOnClickListener { startActivity(Intent(this, ReportActivity::class.java)) }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val list = if (filter == "All") all else all.filter { it.category == filter }
        adapter.submitList(list)

        val (s, e) = monthRange()
        val spent = all.filter { it.date in s..e }.sumOf { it.amount }
        val budget = Prefs.getBudget(this)

        b.tvSpent.text = "₹%.0f".format(spent)
        b.tvBudget.text =
            if (budget > 0) "Budget ₹%.0f  (tap to change)".format(budget) else "Tap to set monthly budget"
        b.progress.progress =
            if (budget > 0) ((spent / budget) * 100).toInt().coerceAtMost(100) else 0
    }

    private fun showBudgetDialog() {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            hint = "10000"
        }
        AlertDialog.Builder(this)
            .setTitle("Monthly budget")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                input.text.toString().toFloatOrNull()?.let { Prefs.setBudget(this, it) }
                refresh()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
