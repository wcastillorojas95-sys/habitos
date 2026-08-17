// Archivo de configuracion raiz del proyecto.
plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
    // Lee google-services.json y genera de él las claves de Firebase, entre ellas
    // default_web_client_id, que es la que necesita el login con Google.
    id("com.google.gms.google-services") version "4.5.0" apply false
}
