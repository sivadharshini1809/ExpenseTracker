package com.example.expensetracker.util

import java.util.Calendar

data class ScanResult(val amount: Double, val merchant: String, val date: Long)

object BillParser {
    private val numRegex = Regex("""\d{1,3}(?:,\d{3})+(?:\.\d{1,2})?|\d+(?:\.\d{1,2})?""")
    private val decimalRegex = Regex("""\d+\.\d{2}""")
    private val dateRegex = Regex("""(\d{1,2})[/\-.](\d{1,2})[/\-.](\d{2,4})""")

    fun parse(text: String): ScanResult {
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }

        // Merchant = first line that has letters
        val merchant = lines.firstOrNull { l -> l.any { it.isLetter() } } ?: ""

        return ScanResult(findAmount(lines), merchant, findDate(text))
    }

    private fun lastNumber(line: String): Double? =
        numRegex.findAll(line).mapNotNull { it.value.replace(",", "").toDoubleOrNull() }.lastOrNull()

    private fun findAmount(lines: List<String>): Double {
        // 1. Line that contains "total" (ignore sub total)
        val idx = lines.indexOfLast {
            it.contains("total", true) && !it.contains("sub", true)
        }
        if (idx >= 0) {
            lastNumber(lines[idx])?.let { return it }
            // Amount may be on the next line
            if (idx + 1 < lines.size) lastNumber(lines[idx + 1])?.let { return it }
        }
        // 2. Fallback: biggest decimal number in the bill
        return decimalRegex.findAll(lines.joinToString("\n"))
            .mapNotNull { it.value.toDoubleOrNull() }
            .filter { it < 1_000_000 }
            .maxOrNull() ?: 0.0
    }

    private fun findDate(text: String): Long {
        val m = dateRegex.find(text) ?: return System.currentTimeMillis()
        val d = m.groupValues[1].toInt()
        val mo = m.groupValues[2].toInt()
        var y = m.groupValues[3].toInt()
        if (y < 100) y += 2000
        if (d !in 1..31 || mo !in 1..12) return System.currentTimeMillis()
        val c = Calendar.getInstance()
        c.set(y, mo - 1, d, 12, 0, 0)
        return c.timeInMillis
    }
}
