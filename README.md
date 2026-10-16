# DFRoot Modern ARM32 UI

**Esta edición sirve para la interfaz Android ARMv7 de 32 bits. No contiene un motor de root funcional.**

La rama `arm32-ui` compila una APK para Android 12+ (API 31) y el ABI `armeabi-v7a`. Incluye una biblioteca JNI ELF32 para verificar que la arquitectura se ha compilado correctamente. Conserva la interfaz Modern, los datos del dispositivo, la consola y los ajustes.

El motor original de DFRoot 4.0 depende de programas y ensamblador ARM64, por lo que no es compatible con un sistema ARMv7 de 32 bits sin un port propio. En esta edición no se empaqueta ni se ejecuta ese motor. La aplicación **no modifica el kernel ni concede root**.

El paquete `df.root.arm32` puede instalarse independientemente del paquete ARM64. Las claves de Supabase no se activan desde esta interfaz porque la función de root no está disponible.

## Descargar

En GitHub Actions de la rama `arm32-ui`, abre la ejecución **DFRoot Modern ARM32 UI** y descarga el artefacto `DFRoot-Modern-ARM32-UI`. Extrae la APK y comprueba el archivo de SHA-256.

El workflow comprueba Android mínimo API 31, ABI `armeabi-v7a`, ELF32 y la ausencia de ejecutables ARM64. La edición con motor ARM64 continúa en la rama `modern-v4.0`.
