package com.lucas.habitos

import java.time.LocalDate

/**
 * Un habito que el usuario quiere sostener en el tiempo.
 *
 * Las fechas cumplidas se guardan como texto ISO ("2026-08-15") dentro de [hechos],
 * asi el guardado en el telefono es simple y no depende de zonas horarias.
 */
data class Habito(
    val id: String,
    val nombre: String,
    val emoji: String,
    val color: Int,
    val creado: String,
    val hechos: Set<String>
) {

    fun hechoEn(fecha: LocalDate): Boolean = hechos.contains(fecha.toString())

    /**
     * Dias consecutivos cumplidos. Si hoy todavia no se marca, la racha se mide
     * hasta ayer: asi no aparece rota a media mañana antes de cumplir el habito.
     */
    fun racha(hoy: LocalDate): Int {
        var dia = if (hechoEn(hoy)) hoy else hoy.minusDays(1)
        var total = 0
        while (hechoEn(dia)) {
            total++
            dia = dia.minusDays(1)
        }
        return total
    }

    /** La racha mas larga alcanzada alguna vez. */
    fun mejorRacha(): Int {
        if (hechos.isEmpty()) return 0
        val fechas = hechos
            .mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }
            .sorted()
        if (fechas.isEmpty()) return 0

        var mejor = 1
        var actual = 1
        for (i in 1 until fechas.size) {
            actual = if (fechas[i - 1].plusDays(1) == fechas[i]) actual + 1 else 1
            if (actual > mejor) mejor = actual
        }
        return mejor
    }

    /** Cuantos de los ultimos [dias] dias (incluyendo hoy) se cumplio. */
    fun cumplidosUltimos(hoy: LocalDate, dias: Int): Int =
        (0 until dias).count { hechoEn(hoy.minusDays(it.toLong())) }
}
