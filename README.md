# GastoClaro

Aplicación Android nativa de contabilidad personal para Colombia (COP). Detecta automáticamente movimientos bancarios desde notificaciones y SMS, distingue transferencias entre tus propias cuentas, y pregunta por tus gastos mediante una burbuja flotante.

**100 % local, sin conexión a internet ni analíticas.** Base de datos cifrada con SQLCipher.

## Stack

- Kotlin, Jetpack Compose, Material 3 (tema oscuro por defecto)
- MVVM + Clean Architecture (paquetes `domain`, `data`, `ui`, `core`)
- Hilt (DI), Room + SQLCipher (persistencia cifrada), DataStore (ajustes), WorkManager-ready, Coroutines + Flow
- Navigation Compose
- Gráficos: implementación propia con `Canvas` de Compose (dona de categorías y barras de tendencia), en lugar de Vico/MPAndroidChart, para un control total del tema oscuro y porque Vico no ofrece gráfico de dona.
- Pruebas unitarias con JUnit + MockK

## Requisitos de compilación

- Android Studio (o JDK 17+) y SDK con `compileSdk 35`.
- `minSdk 26`, `targetSdk 35`.

Compilar desde la línea de comandos (Windows):

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
.\gradlew.bat :app:assembleDebug
```

Ejecutar pruebas unitarias:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

O abrir la carpeta en Android Studio y pulsar *Run*.

## Permisos y concesión en Android

La app necesita permisos que **no** se pueden conceder programáticamente. La pantalla de *Onboarding* guía el proceso.

### 1. Acceso a notificaciones
Ajustes → Notificaciones → Acceso a notificaciones → activa **GastoClaro**. La app solo escucha los paquetes de apps bancarias configurados en *Ajustes*.

### 2. Permisos de SMS
Respaldo para detectar movimientos por mensaje de texto. La app filtra solo por remitentes bancarios configurados.

> **Nota sobre Google Play:** los permisos `RECEIVE_SMS`/`READ_SMS` están restringidos por Play Policy (solo para apps de SMS/llamadas). Para uso personal vía *sideloading* funcionan con normalidad.

### 3. Exclusión de optimización de batería
Evita que el sistema detenga la detección en segundo plano. Algunos fabricantes (Xiaomi, Huawei, etc.) ignoran la solicitud y exigen activarlo manualmente.

### 4. Mostrar sobre otras apps (burbuja flotante)
Necesario para la burbuja de preguntas.

### "Configuración restringida" en Android 13+ (APK fuera de Play Store)
Al instalar un APK descargado fuera de Play Store, Android 13+ puede bloquear el acceso a notificaciones por seguridad:

1. Mantén presionado el ícono de la app → *Información de la aplicación* (o Ajustes → Aplicaciones → GastoClaro).
2. Toca el menú `⋮` (tres puntos) → **Permitir configuración restringida**.
3. Ahora sí, activa el *Acceso a notificaciones*.

## Arquitectura

```
app/src/main/java/com/gastoclaro/
├── core/          # utilidades (Money, fechas, permisos), security, notification, di (Hilt)
├── domain/
│   ├── model/     # Account, Transaction, Category, CategoryRule, Budget, ...
│   ├── repository/# interfaces de repositorios
│   ├── usecase/   # casos de uso (detección, transferencias, burbuja)
│   ├── parser/    # interfaz TransactionParser + implementaciones por banco
│   ├── matching/  # motor de transferencias internas (puro, testeable)
│   └── bubble/    # interpretación de respuestas de la burbuja
├── data/
│   ├── local/     # Room (entidades, DAOs, base cifrada, migraciones)
│   ├── datastore/ # ajustes
│   ├── detection/ # NotificationListenerService, SmsReceiver
│   ├── backup/    # respaldo/restauración cifrada
│   └── repository/# implementaciones
└── ui/            # Compose: tema, navegación, pantallas, burbuja overlay
```

## Cómo agregar un parser nuevo

1. Crea una clase en `domain/parser` que extienda `AbstractBankParser`:

```kotlin
class MiBancoParser @Inject constructor() : AbstractBankParser() {
    override val id: String = "mibanco"
    override val packages: Set<String> = setOf("com.mibanco.app")
    override val senders: Set<String> = setOf("mibanco")
}
```

2. El parser base ya detecta tipo, monto (formato colombiano `1.234.567,89`), fecha/hora, comercio/destinatario, últimos dígitos, referencia y saldo mediante `ParseSupport`. Si tu banco tiene un formato distinto de comercio/destinatario, sobrescribe la lógica en `ParseSupport.extractMerchant` o en tu parser.

3. Regístralo en `core/di/ParserModule.kt` con `@Binds @IntoSet`.

4. Agrega los casos de prueba en `ParserTest.kt` (al menos 5 mensajes realistas).

Los mensajes que ningún parser reconoce se guardan en *Ajustes → Mensajes no reconocidos*, donde puedes crear una regla manual.

## Probar con notificaciones simuladas (modo debug)

*Ajustes → Modo debug*:

- **Mensajes de ejemplo**: toca un banco para cargar un mensaje realista en el campo de texto.
- **Probar parser**: muestra tipo, monto, comercio, cuenta, referencia y saldo extraídos, sin guardar nada.
- **Registrar**: procesa el mensaje de verdad (con deduplicación, emparejamiento de transferencias y cola de burbuja).

Para ver la burbuja sin una transferencia real: registra un mensaje de ejemplo de tipo gasto (p. ej. *Bancolombia → "Compra por $45.900 en EXITO..."*) con el modo de burbuja en *Ajustes → Modo de burbuja → Burbuja flotante* y el permiso *Mostrar sobre otras apps* concedido.

## Transferencias internas

Un movimiento se clasifica como **transferencia propia** si:

1. El destinatario/origen coincide con una cuenta, celular o nombre de titular registrado; o
2. Existe un movimiento opuesto (salida en A, entrada en B) con el mismo monto dentro de la ventana configurable (por defecto 10 min), con tolerancia a comisiones como el 4x1000; o
3. Confirmaste antes ese destinatario como "cuenta mía" (regla aprendida).

Las transferencias internas **no** cuentan como gasto ni ingreso en dashboards, presupuestos ni reportes.

## Migraciones de Room

La base de datos está en versión 1. Para añadir una migración, incrementa `version` en `GastoClaroDatabase`, añade la `Migration` correspondiente y regístrala con `.addMigrations(...)` en `DatabaseModule`.

## Privacidad

- Todo local; sin conexión ni analíticas de terceros.
- Base de datos cifrada con SQLCipher (clave AES-256 en Android Keystore).
- `FLAG_SECURE` en pantallas con saldos (configurable en Ajustes).
- No se guarda el texto de notificaciones ajenas a los bancos configurados.
- Bloqueo biométrico opcional (huella/credencial).
