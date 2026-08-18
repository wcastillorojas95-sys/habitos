package com.lucas.habitos

/**
 * Los iconos de la app.
 *
 * Dos familias, y es a proposito:
 *
 *  - La interfaz (atras, play, papelera, pestanas) usa Material Icons de Google
 *    (Apache 2.0). Es el lenguaje que un usuario de Android ya tiene aprendido;
 *    reinventarlo solo consigue que dude donde no deberia.
 *  - Los iconos de habito usan Solar, estilo Broken (480 Design, CC BY 4.0):
 *    trazo fino con cortes deliberados. Tienen mucho mas caracter que un icono
 *    de sistema, que es justo lo que se le pide a la parte que el usuario elige.
 *
 * Se guardan como recursos y no como ImageVector escrito a mano porque el
 * widget de la pantalla de inicio solo sabe pintar recursos, no vectores de
 * Compose.
 *
 * Cada valor es un id de recurso (Int). En Compose se usan con
 * `painterResource(...)`; en el widget con `setImageViewResource(...)`.
 */

// ---------------------------------------------------------------- interfaz ---

val IconoAtras = R.drawable.ic_arrow_back
val IconoBasura = R.drawable.ic_delete

/** Papelera de línea: más ligera que la maciza para una acción destructiva. */
val IconoBasuraLinea = R.drawable.ic_delete_outline
val IconoCheck = R.drawable.ic_check
val IconoCirculo = R.drawable.ic_radio_button_unchecked
val IconoMarcado = R.drawable.ic_check_circle
val IconoMas = R.drawable.ic_add
val IconoMenos = R.drawable.ic_remove
val IconoPlay = R.drawable.ic_play_arrow
val IconoLuna = R.drawable.ic_bedtime
val IconoLupa = R.drawable.ic_search
val IconoCasa = R.drawable.ic_home
val IconoGrafico = R.drawable.ic_bar_chart
val IconoEngranaje = R.drawable.ic_settings
val IconoMenu = R.drawable.ic_more_horiz
val IconoLapiz = R.drawable.ic_edit
val IconoCerrar = R.drawable.ic_close
val IconoReloj = R.drawable.ic_timer

// ------------------------------------------------------- iconos de hábito ---

/** Una entrada del catálogo: la clave que se guarda y el dibujo que le toca. */
data class IconoHabito(val clave: String, val recurso: Int)

/**
 * El catálogo que ve el usuario al crear un hábito.
 *
 * El orden importa: son seis columnas, así que cada seis entradas forman una
 * fila de la rejilla y se han agrupado por tema (cuerpo, casa, mente, dinero).
 */
val ICONOS_HABITO = listOf(
    IconoHabito("agua", R.drawable.ic_h_agua),
    IconoHabito("correr", R.drawable.ic_h_correr),
    IconoHabito("leer", R.drawable.ic_h_leer),
    IconoHabito("meditar", R.drawable.ic_h_meditar),
    IconoHabito("pesas", R.drawable.ic_h_pesas),
    IconoHabito("comida", R.drawable.ic_h_comida),

    IconoHabito("dormir", R.drawable.ic_h_dormir),
    IconoHabito("musica", R.drawable.ic_h_musica),
    IconoHabito("escribir", R.drawable.ic_h_escribir),
    IconoHabito("limpiar", R.drawable.ic_h_limpiar),
    IconoHabito("medicina", R.drawable.ic_h_medicina),
    IconoHabito("planta", R.drawable.ic_h_planta),

    IconoHabito("nofumar", R.drawable.ic_h_nofumar),
    IconoHabito("sol", R.drawable.ic_h_sol),
    IconoHabito("mente", R.drawable.ic_h_mente),
    IconoHabito("objetivo", R.drawable.ic_h_objetivo),
    IconoHabito("nomovil", R.drawable.ic_h_nomovil),
    IconoHabito("ducha", R.drawable.ic_h_ducha),

    IconoHabito("nota", R.drawable.ic_h_nota),
    IconoHabito("caminar", R.drawable.ic_h_caminar),
    IconoHabito("llamar", R.drawable.ic_h_llamar),
    IconoHabito("cafe", R.drawable.ic_h_cafe),
    IconoHabito("chat", R.drawable.ic_h_chat),
    IconoHabito("cocina", R.drawable.ic_h_cocina),

    IconoHabito("cama", R.drawable.ic_h_cama),
    IconoHabito("casa", R.drawable.ic_h_casa),
    IconoHabito("trabajo", R.drawable.ic_h_trabajo),
    IconoHabito("bandeja", R.drawable.ic_h_bandeja),
    IconoHabito("estudiar", R.drawable.ic_h_estudiar),
    IconoHabito("tiempo", R.drawable.ic_h_tiempo),

    IconoHabito("dinero", R.drawable.ic_h_dinero),
    IconoHabito("nogastar", R.drawable.ic_h_nogastar),
    IconoHabito("grafico", R.drawable.ic_h_grafico),
    IconoHabito("estrella", R.drawable.ic_h_estrella),
    IconoHabito("naturaleza", R.drawable.ic_h_naturaleza),
    IconoHabito("corazon", R.drawable.ic_h_corazon)
)

private val PorClave = ICONOS_HABITO.associateBy { it.clave }

/** Clave por defecto cuando un hábito no trae ninguna o la que trae ya no existe. */
const val ICONO_POR_DEFECTO = "objetivo"

fun recursoDeIcono(clave: String): Int =
    PorClave[clave]?.recurso ?: R.drawable.ic_h_objetivo

/**
 * Emojis de las versiones 1 y 2 traducidos al icono equivalente.
 *
 * Hasta la 2.2 el icono de cada hábito era un emoji guardado tal cual en el
 * JSON. Sin esta tabla, quien actualice la app vería todos sus hábitos con el
 * icono por defecto y perdería la señal visual que ya tenía memorizada.
 */
private val DESDE_EMOJI = mapOf(
    "💧" to "agua",
    "🏃" to "correr",
    "🚶" to "caminar",
    "📖" to "leer",
    "📚" to "estudiar",
    "🧘" to "meditar",
    "💪" to "pesas",
    "🥗" to "comida",
    "🍎" to "comida",
    "🍳" to "cocina",
    "😴" to "dormir",
    "🛏️" to "cama",
    "🎸" to "musica",
    "✍️" to "escribir",
    "🗒️" to "nota",
    "🧹" to "limpiar",
    "💊" to "medicina",
    "🌱" to "planta",
    "🚭" to "nofumar",
    "☀️" to "sol",
    "🧠" to "mente",
    "🎯" to "objetivo",
    "📵" to "nomovil",
    "🚿" to "ducha",
    "📞" to "llamar",
    "☕" to "cafe",
    "💬" to "chat",
    "📥" to "bandeja",
    "🌟" to "estrella",
    "🧾" to "dinero",
    "🚫" to "nogastar",
    "📊" to "grafico",
    "⏳" to "tiempo",
    "✅" to "objetivo"
)

/**
 * Devuelve una clave válida a partir de lo que hubiera guardado: una clave nueva,
 * un emoji viejo, o nada.
 */
fun claveDeIcono(guardado: String?): String {
    val texto = guardado?.trim().orEmpty()
    if (texto.isEmpty()) return ICONO_POR_DEFECTO
    if (PorClave.containsKey(texto)) return texto
    DESDE_EMOJI[texto]?.let { return it }
    // Los emojis con variante (❤️ = ❤ + FE0F) no coinciden por igualdad directa.
    val limpio = texto.filter { it.code != 0xFE0F && it.code != 0x200D }
    DESDE_EMOJI.entries.forEach { (emoji, clave) ->
        if (emoji.filter { it.code != 0xFE0F && it.code != 0x200D } == limpio) return clave
    }
    return ICONO_POR_DEFECTO
}
