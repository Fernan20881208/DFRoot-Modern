# DFRoot Modern · 4.0

Interfaz nativa inspirada en **Material You / Android 16**, sobre el código exacto de [DFRoot v4.0](https://github.com/diabl0w/DFRoot/tree/v4.0), de **diabl0w**.

[![Compilación](https://github.com/Fernan20881208/DFRoot-Modern/actions/workflows/build.yml/badge.svg?branch=modern-v4.0)](https://github.com/Fernan20881208/DFRoot-Modern/actions/workflows/build.yml)

La rama de este rediseño es **`modern-v4.0`**. Base: `923e5a42ce5add6ec13b67137f118b6d5e3c952f`. No incluye cambios del motor de 4.1 o 4.2.

## Interfaz

- Colores del fondo de pantalla y tema claro/oscuro del sistema.
- Tipografía amplia, tarjetas tonales, botones redondeados e interfaz en español.
- Pantallas de inicio y ajustes con márgenes para barras del sistema y recortes de pantalla.
- Selector que muestra gestores con `libksud.so` accesible y permite actualizar la lista.
- Información del dispositivo y consola con copiar/limpiar; conserva el registro y el estado al girar el teléfono.
- Estados diferenciados: sin configurar, en ejecución, error, proceso terminado y marcador detectado.
- Icono adaptable. Paquete independiente `df.root.modern`, instalable junto al original.

La UI usa vistas nativas Java/XML y los colores dinámicos del sistema; no añade Jetpack Compose ni bibliotecas externas. El estilo visual es inspirado en Android 16, no una afirmación de compatibilidad del motor con todos los dispositivos Android 16.

## APK desde GitHub

1. Abre [Actions](https://github.com/Fernan20881208/DFRoot-Modern/actions/workflows/build.yml).
2. Elige una ejecución verde de **DFRoot Modern 4.0** en `modern-v4.0`.
3. Descarga el artifact **DFRoot-Modern-4.0**.
4. Extrae `DFRoot-Modern-4.0-debug.apk`.

El artifact también incluye el código fuente y `SHA256SUMS.txt`. Es una APK debug para pruebas, firmada con una clave de desarrollo. Diferentes ejecuciones de CI pueden generar firmas distintas; no se utiliza ninguna clave privada de producción.

El workflow compila los ocho módulos originales, valida recursos, ejecuta `assembleDebug` y `lintDebug` y comprueba que el APK contenga `libdfroot.so` y `libbootstrap.so` ARM64. La compilación no ejecuta el exploit ni certifica que funcione en un teléfono concreto.

## Sobre `libksud.so not found`

En **DFRoot 4.0**, `libksud.so` proviene del gestor KernelSU que se selecciona; no es una biblioteca que DFRoot deba empaquetar. La versión 4.0 busca el archivo dentro de `ApplicationInfo.nativeLibraryDir` del gestor.

Si no aparece ningún gestor, instala uno compatible y toca **Actualizar**. Si el gestor solo incluye la biblioteca dentro de su APK sin extraerla a esa carpeta, no cumple el contrato del motor original 4.0. Esta UI informa la situación y evita seleccionar otra app cualquiera; no sustituye ni modifica el mecanismo del motor.

## Base y alcance

Se conservan sin cambios `app/src/main/jni/`, `lkm/`, `ExploitRunner.java` y `BootReceiver.java`. CI verifica su igualdad con el commit base. El arranque automático sigue desactivado por defecto. Las preferencias conservan sus claves originales, dentro del almacenamiento de la nueva aplicación.

DFRoot puede congelar o reiniciar dispositivos incompatibles. El rediseño no cambia esa compatibilidad. Abrir la interfaz no inicia el motor; la ejecución manual requiere pulsar el botón y confirmar.

Consulta la [documentación original de v4.0](docs/UPSTREAM-v4.0.md) para requisitos, créditos y funcionamiento del proyecto original. Este fork mantiene la atribución de sus autores y no es una versión oficial.
