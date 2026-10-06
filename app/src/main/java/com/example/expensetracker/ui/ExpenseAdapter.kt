package com.example.expensetracker.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.data.Expense
import com.example.expensetracker.databinding.ItemExpenseBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExpenseAdapter(private val onClick: (Expense) -> Unit) :
    ListAdapter<Expense, ExpenseAdapter.VH>(Diff) {

    class VH(val b: ItemExpenseBinding) : RecyclerView.ViewHolder(b.root)

    private val fmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(h: VH, position: Int) {
        val e = getItem(position)
        h.b.tvMerchant.text = e.merchant.ifEmpty { e.category }
        h.b.tvMeta.text = "${e.category} • ${fmt.format(Date(e.date))}"
        h.b.tvAmount.text = "₹%.0f".format(e.amount)
        h.b.root.setOnClickListener { onClick(e) }
    }

    object Diff : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(a: Expense, b: Expense) = a.id == b.id
        override fun areContentsTheSame(a: Expense, b: Expense) = a == b
    }
}
