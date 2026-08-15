# Hábitos — cómo convertir esto en un APK instalable

Este es el proyecto completo de una app Android para seguir hábitos diarios.
No necesitas instalar nada en tu computadora: **GitHub compila el APK gratis por ti**
y te deja un archivo listo para descargar desde el teléfono.

Tiempo total: unos 10 minutos, y la compilación tarda ~4 minutos.

---

## Paso 1 — Crear el repositorio

1. Entra a [github.com](https://github.com) y crea una cuenta si no tienes (es gratis).
2. Arriba a la derecha, toca **+** → **New repository**.
3. Ponle de nombre `habitos`.
4. **Importante: déjalo en `Public`.** Si lo pones en privado, el teléfono no podrá
   descargar el APK sin iniciar sesión, y además Actions tiene minutos limitados.
5. **No marques** ninguna casilla de "Add a README", "Add .gitignore" ni licencia.
6. Toca **Create repository**.

## Paso 2 — Subir los archivos del proyecto

1. Descomprime el ZIP que te envié. Te quedará una carpeta con `app`, `gradle`,
   `gradlew`, `settings.gradle.kts`, etc.
2. En la página del repositorio recién creado, haz clic en el enlace
   **uploading an existing file**.
3. Abre la carpeta descomprimida, selecciona **todo lo que hay adentro**
   (`Cmd + A`) y arrástralo al recuadro de GitHub.

   > Arrastra **el contenido**, no la carpeta. Si subes la carpeta entera, el
   > compilador no encontrará los archivos y fallará.

4. Abajo, toca el botón verde **Commit changes**.

## Paso 3 — Añadir el archivo que compila el APK

Este archivo va en una carpeta oculta, así que es más fácil crearlo directamente
en la web:

1. En la página principal del repositorio: **Add file** → **Create new file**.
2. En el campo del nombre escribe exactamente esto (con las barras — GitHub creará
   las carpetas solo):

   ```
   .github/workflows/build-apk.yml
   ```

3. Abre el archivo `COPIAR-ESTE-WORKFLOW.txt` del ZIP, copia todo lo que está
   debajo de la línea de guiones y pégalo en el recuadro grande.
4. Toca **Commit changes** → **Commit changes** de nuevo.

En cuanto guardes, la compilación arranca sola.

## Paso 4 — Esperar la compilación

1. Ve a la pestaña **Actions** del repositorio.
2. Verás una tarea llamada *Compilar APK* con un círculo amarillo girando.
3. Espera unos 4 minutos, hasta que salga un ✅ verde.

Si sale una ❌ roja: toca la tarea, luego el paso que falló, copia el texto del
error y mándamelo. Lo corrijo y solo tendrás que volver a subir el archivo
corregido.

## Paso 5 — Instalar en tu Android

1. Desde el **teléfono**, abre el repositorio en Chrome.
2. En la página principal, a la derecha, verás la sección **Releases**. Toca la
   versión `apk`.
3. En **Assets**, toca **Habitos.apk** para descargarlo.
4. Abre el archivo descargado. Android te pedirá permitir instalar apps de esta
   fuente: acepta y activa el permiso para Chrome.
5. Si aparece un aviso de Play Protect ("app no reconocida"), toca
   **Más detalles** → **Instalar de todas formas**. Es normal: solo significa que
   la app no viene de la Play Store.

Listo. El ícono verde aparecerá en tu pantalla de inicio.

---

## Qué hace la app

- Creas hábitos con nombre, ícono y color.
- Los marcas cada día con un toque.
- Calcula la **racha** de días seguidos (🔥) y tu mejor racha histórica.
- Barra de progreso del día y resumen de los últimos 7 días.
- Tira de la semana para revisar o corregir días pasados (los futuros están
  bloqueados).
- Todo se guarda en el teléfono. No pide permisos, no usa internet, no tiene
  cuentas ni publicidad.
- Se adapta al modo oscuro y, en Android 12+, toma los colores de tu fondo de
  pantalla.

## Para cambiar algo después

Dime qué quieres cambiar y te devuelvo el archivo corregido. Lo subes al
repositorio (**Add file → Upload files**, o abres el archivo y tocas el lápiz) y
GitHub recompila el APK automáticamente. Luego lo vuelves a descargar desde
Releases.

Si cambias el número de versión, edita `versionCode` y `versionName` en
`app/build.gradle.kts`; así Android reconoce la nueva instalación como una
actualización y respeta tus datos.

## Detalles técnicos

| | |
|---|---|
| Lenguaje | Kotlin |
| Interfaz | Jetpack Compose + Material 3 |
| Android mínimo | 8.0 (API 26) |
| Compila contra | API 35 |
| Guardado | SharedPreferences con JSON |
| Dependencias externas | ninguna fuera de AndroidX |

El APK que produce es de tipo *debug*: está firmado con la clave de depuración
estándar, que sirve perfectamente para instalarlo tú mismo. Para publicarlo en
Google Play haría falta firmarlo con una clave propia — si algún día llegas ahí,
te ayudo con ese paso.
