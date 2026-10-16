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

## Caso comprobado: Android 12, RTD6748 y kernel 5.4.284+

Diagnóstico ADB comunicado para este equipo:

- Modelo: `Smart TV`; plataforma: `rtd6748`; Android: `12`.
- ABI de **aplicaciones**: `armeabi-v7a,armeabi` (32 bits).
- Kernel: `5.4.284+` (compilación 23-ene-2026). `uname -m`: `armv8l`.
- `armv8l` y una lista de ABI de Android de 32 bits **no prueban por sí solos que el kernel sea de 32 bits**. Algunos sistemas utilizan aplicaciones de 32 bits sobre un kernel ARM64. La configuración activa del kernel es determinante.

### Código fuente publicado con plataforma relacionada

Referencia pública: [TCLOpenSource/rt2875p](https://github.com/TCLOpenSource/rt2875p).
Ofrece `arch/arm64/configs/realtek/android.12.rtd6748_dv.tv030_defconfig`
para Android 12, con `CONFIG_MODULES=y`, `CONFIG_MODVERSIONS=y` y
`CONFIG_IKCONFIG_PROC=y` **en esa configuración de ejemplo**. Su `Makefile`
declara **Linux 5.4.210**, por lo que **NO** es un árbol equivalente al kernel
`5.4.284+` actualmente ejecutado por la TV. No cargar ningún módulo compilado
con esa fuente hasta tener exacta la versión, los símbolos, el `vermagic`,
la arquitectura del kernel y las opciones de configuración del firmware.

### Próxima prueba ADB segura (sin root)

Desde el equipo donde tienes instalado ADB:

```sh
sh scripts/tv_rtd6748_probe.sh
```

El script [tv_rtd6748_probe.sh](scripts/tv_rtd6748_probe.sh) lee propiedades,
`/proc/version`, arquitectura, configuración y metadatos de módulos cuando
son accesibles. No usa `su`, `insmod`, flash ni escrituras a la televisión.

Si no dispones del repositorio clonado, ejecuta estas consultas directas:

```sh
adb shell getprop ro.product.cpu.abilist64
adb shell getprop ro.zygote
adb shell cat /proc/version
adb shell 'if [ -r /proc/config.gz ]; then zcat /proc/config.gz 2>/dev/null | grep -E "^(CONFIG_(ARM64|MODULES|MODVERSIONS|ARCH_RTK6748|IKCONFIG_PROC)=|# CONFIG_(MODULES|ARM64) is not set)"; else echo "CONFIG NO ACCESIBLE"; fi'
adb shell 'ls -ld /sys/kernel/kheaders.tar.xz /vendor/lib/modules /lib/modules 2>/dev/null'
```

**Estado del port**: identificación de la plataforma y fuente relacionada,
no compilación ni carga de un módulo funcional. La edición ARMv7 de TV es
solo interfaz/diagnóstico hasta comprobar una vía técnicamente compatible.

## Compilación

Abre **GitHub Actions → DFRoot Modern Android TV 5.4 diagnostics** en la rama `android-tv-5.4`, descarga `DFRoot-Modern-AndroidTV-ARM32` y extrae la APK. Las comprobaciones verifican el manifest de TV, `armeabi-v7a`, ELF32, API 30, ausencia de ejecutables de root y Android Lint.

El código con motor ARM64 original se conserva en `modern-v4.0`, sin cambios.
