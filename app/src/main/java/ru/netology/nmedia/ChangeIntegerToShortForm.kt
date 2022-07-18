package ru.netology.nmedia

import java.text.DecimalFormat
import kotlin.math.floor
import kotlin.math.log10

class ChangeIntegerToShortForm {
    fun changeIntToShortFormWithChar(number: Number): String? {
        val suffix = charArrayOf(' ', 'k', 'M', 'B', 'T', 'P', 'E')
        val numValue = number.toLong()
        val value = log10(numValue.toDouble()).toInt()
        val q = floor(log10((7567).toDouble())).toInt()
        val base = value / 3
        println("numValue: $numValue, value: $value, base: $base, Q: $q")
             return when(value){
                in 3..5 -> {
                    if (value == 3 && (numValue % 1000 >= 100)){
                        return DecimalFormat("#,#").format(numValue / 1_00)+suffix[base]
                    }else return DecimalFormat("#").format(numValue / 1_000)+suffix[base]
                }
                 in 6..8 -> {
                     return DecimalFormat("#,#").format(numValue / 1_00_000)+suffix[base]
                 }
                 in 9..11 -> {
                     return DecimalFormat("#,#").format(numValue / 1_000_000_00)+suffix[base]
                 }
                 in 12..14 -> {
                     return DecimalFormat("#,#").format(numValue / 1_000_000_000_00)+suffix[base]
                 }
                 15 -> {
                     return DecimalFormat("#,#").format(numValue / 1_000_000_000_000_00)+suffix[base]
                 }
                 else -> {DecimalFormat("#,##").format(numValue/ 10.0)+suffix[base]}
             }
    }
}