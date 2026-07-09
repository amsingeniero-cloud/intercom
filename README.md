# Intercom

App de intercom broadcast para Android (estilo walkie-talkie pero full-duplex) +
servidor de señalización gratuito. Pensado para grupos de hasta 6-8 técnicos,
un único canal (party-line: todos oyen a todos).

## Cómo funciona

- **Audio**: WebRTC en malla (mesh) — cada móvil abre una conexión de audio
  directa con cada otro móvil. No hay servidor de medios, así que el audio
  nunca pasa por infraestructura tuya ni tiene coste.
- **Servidor de señalización** (`server/`): solo pone en contacto a los móviles
  entre sí (intercambia SDP/ICE), nunca toca el audio. Es un WebSocket
  minúsculo en Node.

## 1. Desplegar el servidor de señalización (gratis, Render)

1. Sube la carpeta `server/` (o todo el repo) a un repositorio de GitHub.
2. En [render.com](https://render.com) crea un **Web Service** nuevo, gratis,
   apuntando a ese repo:
   - Root directory: `server`
   - Build command: `npm install`
   - Start command: `npm start`
3. Cuando termine el deploy, Render te da una URL tipo
   `https://intercom-signaling-xxxx.onrender.com`. La app se conecta por
   WebSocket, así que la URL a usar es la misma cambiando `https://` por
   `wss://`.

**Nota**: el plan gratuito de Render "duerme" el servicio tras ~15 min sin
tráfico; la primera conexión del día puede tardar 30-60s en despertar.
Recomendable abrir la app un par de minutos antes de salir en directo, o
dejar algo haciendo ping (p. ej. UptimeRobot gratis) al servicio.

### Probar en local antes de desplegar

Para iterar rápido sin depender de Render:

```
cd server
npm install
npm start
```

Y en `app/build.gradle.kts` pon temporalmente
`SIGNALING_URL = "ws://IP-DE-TU-PC:8080"` (IP de tu PC en la WiFi, no
`localhost`, porque el móvil es otro dispositivo).

## 2. Compilar la app Android

Este entorno no puede ejecutar Gradle (bloqueo de conexión loopback), así
que hay que abrir el proyecto en **Android Studio**:

1. Abre la carpeta `Intercom/` (esta, la raíz) como proyecto en Android Studio.
2. Deja que sincronice Gradle. Si avisa de que falta el wrapper completo
   (`gradlew`/`gradle-wrapper.jar`), acepta la opción de regenerarlo —solo
   se incluyó `gradle-wrapper.properties` con la versión de Gradle a usar.
3. Antes de compilar, edita `app/build.gradle.kts` y pon la URL real de tu
   servidor en `SIGNALING_URL` (con esquema `wss://` si es Render, o
   `ws://` si es tu PC en local).
4. Ejecuta en 2 o más móviles Android (minSdk 26) conectados a internet
   (o a la misma WiFi si usas el servidor local).
5. La primera vez pedirá permiso de micrófono.

### Uso

- Botón verde grande arriba: mantener pulsado para hablar (como un PTT).
- Botón cuadrado "MANOS LIBRES" debajo: al pulsarlo, el micrófono queda
  abierto sin tener que mantener nada pulsado, hasta volver a pulsarlo.

## Estructura

```
Intercom/
  server/                 servidor de señalización (Node + ws)
  app/src/main/java/.../
    MainActivity.kt        UI (botón hablar + manos libres)
    IntercomService.kt      foreground service: mantiene el audio activo
                             con pantalla apagada / app en segundo plano
    WebRTCClient.kt          conexiones WebRTC en malla
    SignalingClient.kt       cliente WebSocket hacia server/index.js
```

## Riesgos conocidos / a revisar en Android Studio

No se pudo compilar en este entorno para verificar contra la librería real,
así que revisa esto al abrir el proyecto:

- `WebRTCClient.kt` usa la interfaz `PeerConnection.Observer` de
  `io.getstream:stream-webrtc-android`. Si Android Studio marca algún método
  de más o de menos (p. ej. `onTrack`), usa Alt+Enter para que lo arregle
  automáticamente — la API es casi idéntica a la librería oficial de Google
  WebRTC, pero puede variar en detalles menores entre versiones.
- Si `stream-webrtc-android:1.1.1` no resuelve en Maven Central, prueba con
  la versión estable más reciente disponible en ese momento.

## Ampliaciones futuras (no incluidas)

- Varios canales/grupos (requeriría selector en la UI y "salas" en el server).
- Más de ~8-10 personas a la vez: pasar de malla P2P a un SFU (servidor que
  mezcla audio), lo cual ya no es enteramente gratis a partir de cierta escala.
- Salida por auricular/Bluetooth con botón dedicado (hoy lo gestiona Android
  automáticamente si hay un dispositivo BT conectado).
