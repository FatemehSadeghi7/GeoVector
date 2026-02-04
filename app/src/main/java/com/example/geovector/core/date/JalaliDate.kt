package com.example.geovector.core.date

import java.util.Calendar
import java.util.GregorianCalendar
import kotlin.math.floor

data class JalaliDate(val jy: Int, val jm: Int, val jd: Int) {
    init {
        require(jm in 1..12) { "Invalid jalali month" }
        require(jd in 1..31) { "Invalid jalali day" }
    }
    fun format(): String = "%04d/%02d/%02d".format(jy, jm, jd)
}

data class GregorianDate(val gy: Int, val gm: Int, val gd: Int)


object JalaliConverter {

    private val breaks = intArrayOf(
        -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181,
        1210, 1635, 2060, 2097, 2192, 2262, 2324, 2394,
        2456, 3178
    )

    private fun div(a: Int, b: Int): Int = floor(a.toDouble() / b.toDouble()).toInt()

    private fun jalCal(jy: Int): Triple<Int, Int, Int> {
        // returns (leap, gy, march)
        val bl = breaks.size
        var gy = jy + 621
        var leapJ = -14
        var jp = breaks[0]
        var jm: Int
        var jump: Int=0

        require(jy >= jp && jy < breaks[bl - 1]) { "Invalid Jalali year: $jy" }

        for (i in 1 until bl) {
            jm = breaks[i]
            jump = jm - jp
            if (jy < jm) break
            leapJ += div(jump, 33) * 8 + div(jump % 33, 4)
            jp = jm
        }

        val n = jy - jp
        leapJ += div(n, 33) * 8 + div((n % 33) + 3, 4)
        if (jump % 33 == 4 && jump - n == 4) leapJ++

        val leapG = div(gy, 4) - div((div(gy, 100) + 1) * 3, 4) - 150
        val march = 20 + leapJ - leapG

        var leap = (n + 1) % 33 - 1
        if (leap < 0) leap += 33
        leap = if (leap % 4 == 0) 1 else 0

        return Triple(leap, gy, march)
    }

    fun jalaliToGregorian(j: JalaliDate): GregorianDate {
        val (leap, gy, march) = jalCal(j.jy)

        val jDayNo = if (j.jm <= 6) {
            (j.jm - 1) * 31 + (j.jd - 1)
        } else {
            6 * 31 + (j.jm - 7) * 30 + (j.jd - 1)
        }

        var gDayNo = jDayNo + march
        var gYear = gy
        var gMonth: Int
        var gDay: Int

        val gLeap = isGregorianLeap(gYear)
        val monthDays = intArrayOf(31, if (gLeap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

        gMonth = 0
        while (gMonth < 12 && gDayNo >= monthDays[gMonth]) {
            gDayNo -= monthDays[gMonth]
            gMonth++
        }

        gDay = gDayNo + 1
        return GregorianDate(gYear, gMonth + 1, gDay)
    }

    private fun isGregorianLeap(gy: Int): Boolean {
        return (gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0)
    }

    fun jalaliToEpochMillis(j: JalaliDate): Long {
        val g = jalaliToGregorian(j)
        val cal: Calendar = GregorianCalendar(g.gy, g.gm - 1, g.gd, 12, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }


    fun computeAgeFromEpochMillis(birthDateMillis: Long): Int {
        val now = Calendar.getInstance()
        val dob = Calendar.getInstance().apply { timeInMillis = birthDateMillis }

        var age = now.get(Calendar.YEAR) - dob.get(Calendar.YEAR)

        val nowDayOfYear = now.get(Calendar.DAY_OF_YEAR)
        val dobDayOfYear = dob.get(Calendar.DAY_OF_YEAR)
        if (nowDayOfYear < dobDayOfYear) age -= 1

        return age
    }
}
