package com.example.expensetracker.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.expensetracker.databinding.ActivityReportBinding
import com.example.expensetracker.util.monthRange
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReportActivity : AppCompatActivity() {

    private lateinit var b: ActivityReportBinding
    private val vm: ExpenseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityReportBinding.inflate(layoutInflater)
        setContentView(b.root)
        title = "Reports"

        vm.expenses.observe(this) { list ->
            // Pie chart: this month, by category
            val (s, e) = monthRange()
            val entries = list.filter { it.date in s..e }
                .groupBy { it.category }
                .map { PieEntry(it.value.sumOf { x -> x.amount }.toFloat(), it.key) }
            val pieSet = PieDataSet(entries, "").apply {
                colors = ColorTemplate.MATERIAL_COLORS.toList()
                valueTextSize = 12f
            }
            b.pie.data = PieData(pieSet)
            b.pie.centerText = "This month"
            b.pie.description.isEnabled = false
            b.pie.invalidate()

            // Bar chart: last 6 months
            val fmt = SimpleDateFormat("MMM", Locale.getDefault())
            val labels = mutableListOf<String>()
            val bars = mutableListOf<BarEntry>()
            for (i in 5 downTo 0) {
                val c = Calendar.getInstance()
                c.add(Calendar.MONTH, -i)
                val key = c.get(Calendar.YEAR) * 12 + c.get(Calendar.MONTH)
                val sum = list.filter {
                    val cc = Calendar.getInstance().apply { timeInMillis = it.date }
                    cc.get(Calendar.YEAR) * 12 + cc.get(Calendar.MONTH) == key
                }.sumOf { it.amount }
                bars.add(BarEntry((5 - i).toFloat(), sum.toFloat()))
                labels.add(fmt.format(c.time))
            }
            b.bar.data = BarData(BarDataSet(bars, "Monthly spend (₹)"))
            b.bar.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            b.bar.xAxis.position = XAxis.XAxisPosition.BOTTOM
            b.bar.xAxis.granularity = 1f
            b.bar.description.isEnabled = false
            b.bar.invalidate()
        }
    }
}
