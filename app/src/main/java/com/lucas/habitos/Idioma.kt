package com.lucas.habitos

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * El idioma de la interfaz.
 *
 * Los textos NO viven en strings.xml sino junto al codigo, con la funcion [t].
 * Es una decision deliberada y conviene explicarla, porque va contra lo que
 * recomienda Android:
 *
 *  - strings.xml es el mecanismo correcto cuando el idioma lo elige el sistema.
 *    Aqui lo elige el usuario con un boton dentro de la app, y cambiarlo en
 *    caliente con recursos obliga a recrear la Activity entera.
 *  - Con t("Hoy", "Today") el original y su traduccion se leen juntos. Con
 *    claves (R.string.hoy) hay que saltar a otro archivo para saber que dice
 *    cada una, y las que sobran no las descubre nadie.
 *
 * A cambio se pierde la traduccion automatica por locale del sistema. Como el
 * requisito era un boton, el cambio compensa.
 *
 * Al ser un estado de Compose, tocar el boton repinta toda la interfaz al
 * instante, sin reiniciar nada.
 */
object Idioma {

    var ingles by mutableStateOf(false)
        private set

    fun cargar(almacen: AlmacenEnfoque) {
        ingles = almacen.enIngles
    }

    fun alternar(almacen: AlmacenEnfoque) {
        ingles = !ingles
        almacen.enIngles = ingles
    }

    /** Etiqueta corta para el boton: enseña a que idioma se cambiaria. */
    val siguiente: String get() = if (ingles) "ES" else "EN"
}

/**
 * Elige entre el texto en español y el ingles.
 *
 * Leer [Idioma.ingles] dentro de una composicion la suscribe al cambio, asi que
 * cualquier Text que use esto se repinta solo al pulsar el boton.
 */
fun t(es: String, en: String): String = if (Idioma.ingles) en else es
