package com.lucas.habitos

import java.time.LocalDate

/** Cada cuanto toca cumplir el habito. */
enum class Frecuencia { DIARIO, DIAS_SEMANA, VECES_SEMANA, CADA_N_DIAS }

/** Como se mide el cumplimiento de un dia. */
enum class Meta { SI_NO, CANTIDAD, TIEMPO }

/**
 * Un habito.
 *
 * [registros] guarda fecha ISO -> cantidad acumulada ese dia. Para los habitos
 * de si/no la cantidad es 0 o 1. [descansos] son dias marcados a proposito como
 * libres: no suman, pero tampoco rompen la racha.
 */
data class Habito(
    val id: String,
    val nombre: String,
    /** Clave del catálogo de [ICONOS_HABITO], no un emoji. */
    val icono: String,
    val color: Int,
    val creado: String,
    val categoria: String = "",
    val archivado: Boolean = false,

    val frecuencia: Frecuencia = Frecuencia.DIARIO,
    val diasSemana: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 7),
    val vecesPorSemana: Int = 3,
    val cadaNDias: Int = 2,

    val meta: Meta = Meta.SI_NO,
    val metaCantidad: Int = 1,
    val unidad: String = "",

    val recordatorio: Boolean = false,
    val recordatorioMinutos: Int = 8 * 60,
    /** Minutos de antelación de los avisos previos: {30, 10} = media hora y 10 min antes. */
    val avisosPrevios: Set<Int> = emptySet(),

    val enCalendario: Boolean = false,
    /** Id del evento repetitivo en el calendario del teléfono. 0 = no tiene. */
    val eventoCalendario: Long = 0L,

    val registros: Map<String, Int> = emptyMap(),
    val descansos: Set<String> = emptySet()
) {

    fun fechaCreado(): LocalDate =
        runCatching { LocalDate.parse(creado) }.getOrDefault(LocalDate.now())

    /** Cuanto hay que acumular en un dia para darlo por cumplido. */
    fun objetivoDiario(): Int = if (meta == Meta.SI_NO) 1 else maxOf(1, metaCantidad)

    fun progreso(fecha: LocalDate): Int = registros[fecha.toString()] ?: 0

    fun cumplido(fecha: LocalDate): Boolean = progreso(fecha) >= objetivoDiario()

    fun esDescanso(fecha: LocalDate): Boolean = descansos.contains(fecha.toString())

    /** Si el habito esta programado para ese dia segun su frecuencia. */
    fun aplicaEn(fecha: LocalDate): Boolean = when (frecuencia) {
        Frecuencia.DIARIO -> true
        Frecuencia.VECES_SEMANA -> true
        Frecuencia.DIAS_SEMANA -> diasSemana.contains(fecha.dayOfWeek.value)
        Frecuencia.CADA_N_DIAS -> {
            val n = maxOf(1, cadaNDias)
            val dias = java.time.temporal.ChronoUnit.DAYS.between(fechaCreado(), fecha)
            dias >= 0 && dias % n == 0L
        }
    }

    /** Texto corto del progreso: "3/8 vasos", "12/30 min" o vacio si es si/no. */
    fun textoProgreso(fecha: LocalDate): String = when (meta) {
        Meta.SI_NO -> ""
        Meta.CANTIDAD -> "${progreso(fecha)}/${objetivoDiario()} ${unidad}".trim()
        Meta.TIEMPO -> "${progreso(fecha)}/${objetivoDiario()} min"
    }

    fun descripcionFrecuencia(): String = when (frecuencia) {
        Frecuencia.DIARIO -> t("Todos los días", "Every day")
        Frecuencia.VECES_SEMANA -> t("$vecesPorSemana veces por semana", "$vecesPorSemana times per week")
        Frecuencia.CADA_N_DIAS ->
            if (cadaNDias == 2) t("Día por medio", "Every other day")
            else t("Cada $cadaNDias días", "Every $cadaNDias days")
        Frecuencia.DIAS_SEMANA -> {
            if (diasSemana.size == 7) t("Todos los días", "Every day")
            else diasSemana.sorted().joinToString(" ") { LETRAS_DIA[it - 1] }
        }
    }

    // ---------- rachas ----------

    /**
     * Dias seguidos cumpliendo. Los dias que la frecuencia no pide y los
     * descansos se saltan sin romper nada. Si hoy todavia esta pendiente, la
     * racha se mide hasta ayer para no mostrarla rota a media mañana.
     */
    fun racha(hoy: LocalDate): Int {
        if (frecuencia == Frecuencia.VECES_SEMANA) return rachaSemanal(hoy)

        var dia = hoy
        if (aplicaEn(hoy) && !cumplido(hoy) && !esDescanso(hoy)) dia = hoy.minusDays(1)

        val limite = fechaCreado()
        var total = 0
        var vueltas = 0
        while (!dia.isBefore(limite) && vueltas < 4000) {
            vueltas++
            when {
                esDescanso(dia) -> {}
                !aplicaEn(dia) -> {}
                cumplido(dia) -> total++
                else -> return total
            }
            dia = dia.minusDays(1)
        }
        return total
    }

    private fun rachaSemanal(hoy: LocalDate): Int {
        var lunes = inicioSemana(hoy)
        if (vecesEnSemana(lunes) < maxOf(1, vecesPorSemana)) lunes = lunes.minusWeeks(1)

        var total = 0
        var vueltas = 0
        while (vecesEnSemana(lunes) >= maxOf(1, vecesPorSemana) && vueltas < 520) {
            total++
            lunes = lunes.minusWeeks(1)
            vueltas++
        }
        return total
    }

    /** Cuantos dias de esa semana se cumplieron. */
    fun vecesEnSemana(cualquierDia: LocalDate): Int {
        val lunes = inicioSemana(cualquierDia)
        return (0L..6L).count { cumplido(lunes.plusDays(it)) }
    }

    /** La racha mas larga alcanzada alguna vez. */
    fun mejorRacha(hoy: LocalDate): Int {
        var dia = fechaCreado()
        var mejor = 0
        var actual = 0
        var vueltas = 0
        while (!dia.isAfter(hoy) && vueltas < 4000) {
            vueltas++
            when {
                esDescanso(dia) -> {}
                !aplicaEn(dia) -> {}
                cumplido(dia) -> {
                    actual++
                    if (actual > mejor) mejor = actual
                }
                else -> actual = 0
            }
            dia = dia.plusDays(1)
        }
        return mejor
    }

    /** Porcentaje de dias programados que se cumplieron en la ventana pedida. */
    fun constancia(hoy: LocalDate, dias: Int): Int {
        var programados = 0
        var hechos = 0
        for (i in 0 until dias) {
            val d = hoy.minusDays(i.toLong())
            if (d.isBefore(fechaCreado())) break
            if (esDescanso(d) || !aplicaEn(d)) continue
            programados++
            if (cumplido(d)) hechos++
        }
        return if (programados == 0) 0 else hechos * 100 / programados
    }

    companion object {
        fun inicioSemana(f: LocalDate): LocalDate = f.minusDays((f.dayOfWeek.value - 1).toLong())
    }
}

/*
 * Los nombres de dias y meses son propiedades calculadas, no constantes.
 *
 * Con "val X = listOf(...)" la lista se crea una vez al cargar la clase y se
 * quedaria en el idioma que hubiera entonces. Con "get()" se recalcula en cada
 * lectura, y como leer Idioma.ingles dentro de una composicion la suscribe al
 * cambio, tocar el boton repinta las fechas al instante. Y lo mejor: ningun
 * sitio que las use tiene que cambiar una linea.
 */
val LETRAS_DIA: List<String>
    get() = if (Idioma.ingles) listOf("M", "T", "W", "T", "F", "S", "S")
    else listOf("L", "M", "X", "J", "V", "S", "D")

val NOMBRES_DIA: List<String>
    get() = if (Idioma.ingles) listOf(
        "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    ) else listOf(
        "lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo"
    )

val MESES: List<String>
    get() = if (Idioma.ingles) listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    ) else listOf(
        "enero", "febrero", "marzo", "abril", "mayo", "junio",
        "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
    )

fun fechaLarga(f: LocalDate): String =
    if (Idioma.ingles) "${NOMBRES_DIA[f.dayOfWeek.value - 1]}, ${f.dayOfMonth} ${MESES[f.monthValue - 1]}"
    else "${NOMBRES_DIA[f.dayOfWeek.value - 1]} ${f.dayOfMonth} de ${MESES[f.monthValue - 1]}"

fun horaTexto(minutos: Int): String {
    val h = (minutos / 60).coerceIn(0, 23)
    val m = (minutos % 60).coerceIn(0, 59)
    return String.format("%02d:%02d", h, m)
}
