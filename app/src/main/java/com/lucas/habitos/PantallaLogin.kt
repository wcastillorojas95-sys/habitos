package com.lucas.habitos

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

/**
 * La puerta de entrada.
 *
 * Se enseña mientras no haya sesión de Firebase ni se haya elegido entrar sin
 * cuenta. En cuanto una de las dos cosas ocurre, MainActivity cambia a la app.
 */
@Composable
fun PantallaLogin(onEntrarSinCuenta: () -> Unit) {
    val contexto = LocalContext.current

    val lanzador = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        // Google no informa del fallo por el código de resultado sino por la
        // excepción que trae dentro del Intent, así que hay que desenvolverla.
        try {
            val cuentaGoogle = GoogleSignIn
                .getSignedInAccountFromIntent(resultado.data)
                .getResult(ApiException::class.java)

            val token = cuentaGoogle?.idToken
            if (token.isNullOrBlank()) {
                SesionUsuario.entrando = false
                SesionUsuario.error = "Google no devolvió el token de identidad."
                return@rememberLauncherForActivityResult
            }

            Autenticacion.entrarConGoogle(token) { cuenta, fallo ->
                SesionUsuario.entrando = false
                SesionUsuario.cuenta = cuenta
                SesionUsuario.error = fallo
            }
        } catch (e: ApiException) {
            SesionUsuario.entrando = false
            // Cancelar no es un fallo: si lo pintáramos en rojo daría la
            // sensación de que algo se ha roto por cerrar el selector.
            SesionUsuario.error = Autenticacion.mensajeDeFallo(e).ifBlank { null }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Spacer(Modifier.height(60.dp))
        DespertadorNaranja(modifier = Modifier.size(150.dp))

        Spacer(Modifier.height(28.dp))
        Text(
            text = "Hábitos",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Entra con tu cuenta para tener tu nombre y tu perfil en la app.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(40.dp))

        BotonEntrar(
            icono = R.drawable.ic_google,
            texto = "Continuar con Google",
            cargando = SesionUsuario.entrando,
            onClick = {
                SesionUsuario.error = null
                SesionUsuario.entrando = true
                lanzador.launch(Autenticacion.cliente(contexto).signInIntent)
            }
        )

        SesionUsuario.error?.let { fallo ->
            Spacer(Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.10f))
                    .padding(14.dp)
            ) {
                Text(
                    text = fallo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(30.dp))

        // Salida de emergencia deliberada: si el login falla por algo del
        // teléfono, nadie debería quedarse fuera de sus propios hábitos, que
        // están guardados aquí y no en ningún servidor.
        Text(
            text = "Entrar sin cuenta",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .clickable { onEntrarSinCuenta() }
                .padding(horizontal = 18.dp, vertical = 12.dp)
        )

        Spacer(Modifier.height(14.dp))
        Text(
            text = "Tus hábitos se guardan en este teléfono. La cuenta sirve para " +
                "identificarte, no sube nada a internet.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun BotonEntrar(
    icono: Int,
    texto: String,
    cargando: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(enabled = !cargando) { onClick() }
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (cargando) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(14.dp))
            Text(
                text = "Entrando…",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        } else {
            Icon(
                painter = painterResource(icono),
                contentDescription = null,
                tint = androidx.compose.ui.graphics.Color.Unspecified,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(14.dp))
            Text(
                text = texto,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
