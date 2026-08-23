package com.exoduss.cronos.util

import java.time.LocalDate
import java.time.YearMonth

object BrazilHolidays {

    fun getHolidaysForDate(date: LocalDate): List<String> =
        getHolidaysForYear(date.year).filter { it.first == date }.map { it.second }

    fun getHolidaysForMonth(yearMonth: YearMonth): List<Pair<LocalDate, String>> =
        getHolidaysForYear(yearMonth.year).filter { it.first.month == yearMonth.month }

    private fun easterDate(year: Int): LocalDate {
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = ((h + l - 7 * m + 114) % 31) + 1
        return LocalDate.of(year, month, day)
    }

    private fun getHolidaysForYear(year: Int): List<Pair<LocalDate, String>> {
        val easter = easterDate(year)
        return listOf(
            LocalDate.of(year, 1, 1)     to "Confraternização Universal (Ano Novo)",
            LocalDate.of(year, 4, 21)    to "Tiradentes",
            LocalDate.of(year, 5, 1)     to "Dia do Trabalho",
            LocalDate.of(year, 9, 7)     to "Independência do Brasil",
            LocalDate.of(year, 10, 12)   to "Nossa Senhora Aparecida",
            LocalDate.of(year, 11, 2)    to "Finados",
            LocalDate.of(year, 11, 15)   to "Proclamação da República",
            LocalDate.of(year, 11, 20)   to "Dia da Consciência Negra",
            LocalDate.of(year, 12, 25)   to "Natal",
            easter.minusDays(47)         to "Carnaval (Segunda-feira)",
            easter.minusDays(46)         to "Carnaval (Terça-feira)",
            easter.minusDays(2)          to "Sexta-feira Santa",
            easter                       to "Páscoa",
            easter.plusDays(60)          to "Corpus Christi"
        ).sortedBy { it.first }
    }
}
