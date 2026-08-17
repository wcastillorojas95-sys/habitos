package com.lucas.habitos

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

/** Los datos de quien ha entrado. Solo lo que hace falta para saludarle. */
data class Cuenta(
    val uid: String,
    val nombre: String,
    val correo: String,
    val foto: String?
) {
    /** "Lucas" a partir de "Lucas Castillo Rojas". */
    fun nombrePila(): String = nombre.trim().substringBefore(" ").ifBlank { "" }
}

/**
 * Quién está dentro de la app, en memoria.
 *
 * Firebase ya guarda la sesión en disco y la renueva sola, así que esto no es la
 * fuente de verdad: es la copia que Compose puede observar para redibujar cuando
 * alguien entra o sale.
 */
object SesionUsuario {
    var cuenta by mutableStateOf<Cuenta?>(null)

    /** Ha elegido usar la app sin cuenta. Se recuerda entre arranques. */
    var invitado by mutableStateOf(false)

    var entrando by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    val dentro: Boolean get() = cuenta != null || invitado
}

/**
 * Entrar con Google.
 *
 * Son dos pasos encadenados que se confunden a menudo: Google devuelve un
 * idToken que solo dice "esta persona es quien dice ser", y ese token se
 * cambia por una sesión de Firebase, que es la que de verdad persiste.
 */
object Autenticacion {

    fun cliente(contexto: Context): GoogleSignInClient {
        val opciones = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            // El id del cliente web lo genera el plugin de google-services a
            // partir de google-services.json. Escribirlo a mano significaría que
            // cambiar de proyecto en Firebase rompe el login sin avisar.
            .requestIdToken(contexto.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(contexto, opciones)
    }

    /** La sesión que Firebase tuviera guardada de la última vez. */
    fun actual(): Cuenta? = FirebaseAuth.getInstance().currentUser?.let(::deFirebase)

    fun entrarConGoogle(idToken: String, onHecho: (Cuenta?, String?) -> Unit) {
        val credencial = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credencial)
            .addOnSuccessListener { resultado ->
                val usuario = resultado.user
                if (usuario == null) onHecho(null, "Firebase aceptó la cuenta pero no devolvió usuario.")
                else onHecho(deFirebase(usuario), null)
            }
            .addOnFailureListener { fallo ->
                onHecho(null, fallo.localizedMessage ?: "No se pudo completar la entrada.")
            }
    }

    fun salir(contexto: Context) {
        FirebaseAuth.getInstance().signOut()
        // Sin esto, Google recuerda la última cuenta y volver a entrar no deja
        // elegir otra: al pulsar el botón entraría directo con la de siempre.
        runCatching { cliente(contexto).signOut() }
        SesionUsuario.cuenta = null
    }

    private fun deFirebase(usuario: com.google.firebase.auth.FirebaseUser) = Cuenta(
        uid = usuario.uid,
        nombre = usuario.displayName.orEmpty(),
        correo = usuario.email.orEmpty(),
        foto = usuario.photoUrl?.toString()
    )

    /**
     * Traduce los códigos de Google a algo que se pueda leer.
     *
     * El 10 es el que se lleva a todo el mundo por delante: significa que la
     * huella SHA-1 del APK no está registrada en Firebase, y el mensaje que trae
     * de fábrica no lo dice por ningún lado.
     */
    fun mensajeDeFallo(e: ApiException): String = when (e.statusCode) {
        12501 -> ""                       // lo canceló el usuario: no es un error
        7 -> "Sin conexión. Comprueba internet e inténtalo otra vez."
        10 -> "La firma de esta versión no está registrada en Firebase. " +
            "Hay que añadir su huella SHA-1 al proyecto."
        12500 -> "Este teléfono no tiene los servicios de Google al día."
        else -> "No se pudo entrar con Google (código ${e.statusCode})."
    }
}
