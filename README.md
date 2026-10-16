# DFRoot Modern — Android TV / kernel 5.4.284

**Estado: APK de diagnóstico Android TV (32 bits), sin exploit/root.** Un kernel 5.4.284 no se vuelve compatible con DFRoot cambiando solo el identificador de kernel.

## Qué está implementado

- Rama independiente: `android-tv-5.4`, basada en `arm32-ui`.
- Android TV 11+ (API 30+), `armeabi-v7a`, interfaz con D-pad (flechas y OK), fuentes y controles grandes.
- Icono, banner de 320×180 y categoría `LEANBACK_LAUNCHER` para aparecer en Android TV.
- Diagnóstico del modelo, SoC/hardware, ABI de usuarios, Android, versión exacta de kernel y parche de seguridad; permite copiarlo al portapapeles.
- Reconocimiento del kernel `5.4.284` solamente como **coincidencia informativa**. Nunca anuncia root.
- Paleta de reserva para Android 11 y colores dinámicos Material You en Android 12+.
- Paquete separado `df.root.arm32.tv`, por lo que no sustituye los otros builds.
- No hay ejecutor de root ni `libdfroot.so`/bootstrap. Se conserva el diseño de la UI y código cliente Supabase sin canjear licencias para una función inexistente.

## Por qué todavía no funciona el root en kernel 5.4.284

La versión original usa payloads AArch64, `crash_dump64`, bibliotecas `/system/lib64/` y módulos `.ko` compilados para familias GKI 5.10 en adelante. El kernel 5.4.284 es otra familia, posiblemente con modificaciones del fabricante. No se puede cargar un módulo 5.10 en 5.4 ni convertir un exploit AArch64 a ARMv7 renombrándolo. Hace falta identificar SoC, ABI real del kernel, Android del fabricante, código fuente/configuración del kernel y una vía compatible y comprobada de carga, antes de evaluar un port nativo.

## Datos necesarios para investigar el kernel 5.4.284

Puedes copiarlos desde la pantalla principal de la APK o, con ADB conectado a la TV, ejecutar sin root:

```sh
adb shell getprop ro.product.model
adb shell getprop ro.build.version.release
adb shell getprop ro.product.cpu.abilist
adb shell getprop ro.board.platform
adb shell getprop ro.hardware
adb shell uname -a
adb shell cat /proc/version
```

Si la TV ejecuta Android 10 o anterior, esta APK todavía no podrá instalarse: habría que reducir `minSdk` y adaptar las APIs de interfaz. No asumas Android 11 solo por tener kernel 5.4.

## Compilación

Abre **GitHub Actions → DFRoot Modern Android TV 5.4 diagnostics** en la rama `android-tv-5.4`, descarga `DFRoot-Modern-AndroidTV-ARM32` y extrae la APK. Las comprobaciones verifican el manifest de TV, `armeabi-v7a`, ELF32, API 30, ausencia de ejecutables de root y Android Lint.

El código con motor ARM64 original se conserva en `modern-v4.0`, sin cambios.
