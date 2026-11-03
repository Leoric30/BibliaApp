# BibleSearchUM

Aplicación móvil Android, desarrollada con **Kotlin** y **Jetpack Compose**, para consultar versículos de la Biblia (Reina-Valera 1960). La persona usuaria elige el **libro**, el **capítulo** y el **versículo** en tres menús desplegables que dependen uno del otro, y la app trae el texto en tiempo real desde la API REST pública de [Bolls Bible](https://bolls.life/).

> Proyecto de la práctica de laboratorio de **Desarrollo de Aplicaciones Móviles** — Universidad de Montemorelos.

📄 El documento de presentación completo está en [`BibleSearchUM_Presentacion.pdf`](BibleSearchUM_Presentacion.pdf).

---

## Características

- **Selectores en cascada** (Libro → Capítulo → Versículo) hechos con `ExposedDropdownMenuBox` de Material 3. Al cambiar un nivel, se limpian los niveles inferiores.
- **Consumo de una API REST** con Retrofit 2, Gson y corrutinas de Kotlin, sin bloquear el hilo principal.
- **Arquitectura MVVM**: un `ViewModel` expone un único `StateFlow<BibleUiState>` inmutable.
- **Estados de UI explícitos**: indicador de carga, tarjeta de error (con botón *Reintentar*) y tarjeta con el versículo.
- **Número real de versículos por capítulo**: se consulta el capítulo para mostrar solo los versículos que existen. Si esa consulta falla, se usa un máximo de 150.
- **Compartir el versículo** (Reto 1) con un `Intent.ACTION_SEND` implícito hacia WhatsApp, Telegram, correo, etc.
- **Botón Reiniciar** en la barra superior, que limpia la selección.
- Tema propio con soporte para **modo oscuro** e ícono adaptativo.

## Tecnologías

| Área | Tecnología |
|---|---|
| Lenguaje | Kotlin 2.2 |
| UI | Jetpack Compose + Material 3 (BOM 2024.02.01) |
| Arquitectura | MVVM: `ViewModel` + `StateFlow` |
| Red | Retrofit 2.9 + Gson converter |
| Concurrencia | Kotlin Coroutines 1.7.3 |
| Build | Gradle 9.1 (wrapper) + Android Gradle Plugin 9.0.1 |
| SDK | `minSdk 26` (Android 8.0) · `targetSdk 36` |

## Estructura del repositorio

```
.
├── README.md                         ← este archivo
├── BibleSearchUM_Presentacion.pdf    ← documento de presentación
└── BibleSearchUM/                    ← proyecto de Android Studio (abrir esta carpeta)
    ├── build.gradle.kts
    ├── settings.gradle.kts
    ├── gradlew / gradlew.bat
    └── app/src/main/
        ├── AndroidManifest.xml       (permiso INTERNET)
        └── java/com/example/blesearchum/
            ├── MainActivity.kt
            ├── model/BibleModels.kt          (BibleBook, BibleVerse)
            ├── network/BibleApiService.kt    (interfaz Retrofit + RetrofitInstance)
            └── ui/
                ├── BibleUiState.kt
                ├── BibleViewModel.kt
                ├── BibleSearchScreen.kt
                ├── components/DropdownSelector.kt
                └── theme/Theme.kt
```

## Cómo funciona

```
 BibleSearchScreen (Compose)  ──eventos──▶  BibleViewModel  ──suspend──▶  BibleApiService (Retrofit)
          ▲                                     │                                │
          └──── collectAsStateWithLifecycle ◀── StateFlow<BibleUiState>     https://bolls.life/
```

1. Al crearse, el `ViewModel` llama a `fetchBooks()` y carga los 66 libros.
2. Al elegir un **libro** se generan sus capítulos (`1..book.chapters`) y se limpian capítulo y versículo.
3. Al elegir un **capítulo** se consulta `get-text/` para saber cuántos versículos tiene.
4. Al elegir un **versículo** se llama a `get-verse/`. El texto se limpia de etiquetas HTML y se muestra en una tarjeta.
5. Cualquier fallo de red cambia `errorMessage` y la UI muestra una tarjeta de error.

### Endpoints usados

| Uso | Método y ruta |
|---|---|
| Lista de libros | `GET https://bolls.life/get-books/RV1960/` |
| Un versículo | `GET https://bolls.life/get-verse/RV1960/{bookId}/{chapter}/{verse}/` |
| Capítulo completo (para contar versículos) | `GET https://bolls.life/get-text/RV1960/{bookId}/{chapter}/` |

> **Nota:** la guía de laboratorio usa el código `RVR1960`, pero hoy la API responde *"There is no such translation: RVR1960"*. El código vigente para la Reina-Valera 1960 es **`RV1960`**, y es el que usa la app.

## Cómo ejecutarla

**Requisitos:** Android Studio (Jellyfish o superior), JDK 17 o más reciente y un emulador o dispositivo con Android 8.0+ y conexión a Internet.

1. Clona el repositorio:
   ```bash
   git clone <url-del-repositorio>
   ```
2. En Android Studio elige **File → Open** y selecciona la carpeta **`BibleSearchUM/`** (la que tiene `settings.gradle.kts`).
3. Espera a que termine el *Gradle Sync*. Android Studio crea `local.properties` con la ruta de tu SDK.
4. Ejecuta la configuración **app** (▶) en un emulador o dispositivo.

Desde la terminal:

```bash
cd BibleSearchUM
./gradlew assembleDebug      # genera app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug       # la instala en el dispositivo conectado
```

## Mejoras futuras

- **Favoritos sin conexión** (Reto 2): guardar versículos con Room o DataStore y verlos en otra pestaña.
- **Selector de traducción** (Reto 3): elegir entre RV1960, NVI, NTV, LBLA, etc.
- **Lectura del capítulo completo** y rangos de versículos (por ejemplo, Juan 3:16-18).
- **Búsqueda por palabra clave** en el texto bíblico.
- **Caché local** de libros y capítulos para usar la app sin conexión.
- **Inyección de dependencias** (Hilt) y una capa `Repository` para facilitar las pruebas.
- **Pruebas unitarias** del `ViewModel` (con un fake de la API) y pruebas de UI con Compose Testing.
- Accesibilidad: lectura en voz alta (Text-to-Speech) y tamaño de letra ajustable.
- Versículo del día con notificación diaria (WorkManager).

## Créditos

- Texto bíblico: Reina-Valera 1960, obtenido de la API pública de [Bolls Bible](https://bolls.life/).
- Desarrollado como práctica académica de la asignatura *Desarrollo de Aplicaciones Móviles*.
