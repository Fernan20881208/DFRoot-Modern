# DFRoot Modern — ARM64 / motor original v4.5

[![Build ARM64 4.5](https://github.com/Fernan20881208/DFRoot-Modern/actions/workflows/build.yml/badge.svg?branch=modern-v4.5)](https://github.com/Fernan20881208/DFRoot-Modern/actions/workflows/build.yml)

Aplicación Android con interfaz inspirada en Material You, basada en los componentes de **[DFRoot oficial v4.5](https://github.com/diabl0w/DFRoot/releases/tag/v4.5)** (autor: diabl0w), y un control de licencias remoto usando Supabase. La rama de trabajo es **`modern-v4.5`**.

Esta edición es **exclusivamente ARM64 (`arm64-v8a`)**. Se mantienen las ramas `modern-v4.0`, `arm32-ui` y `android-tv-5.4` sin mezclarlas con esta actualización.

## Motor v4.5

Los archivos de motor `app/src/main/jni/`, `lkm/` y `app/src/main/java/df/root/ExploitRunner.java` provienen de **la etiqueta oficial v4.5**, no de `master` ni de v4.0. Se actualizaron las partes que cambiaron: `bootstrap.c`, `elf_parser.c`, `exp.c`, `libcxx.S`, `lkm/dfroot.c`, `ExploitRunner.java`. La compilación verifica automáticamente que la fuente del motor coincide exactamente con la etiqueta v4.5 publicada por upstream.

- `applicationId = "df.root"` para conservar las rutas internas del motor original.
- `buildFeatures.buildConfig = true`, requerido por el `ExploitRunner` de v4.5.
- Arquitectura: ARM64. Android mínimo: Android 12 (API 31) **para instalación de la APK**; no garantiza compatibilidad del motor con un firmware específico.
- Se conserva el inicio manual con confirmación; el arranque automático está deshabilitado para no eludir la verificación online.
- Ejecutar DFRoot en kernels o parches no compatibles puede causar bloqueos o reinicios. **No usar la compilación como evidencia de vulnerabilidad/compatibilidad.**

## Keys de 1 hora y permanentes

**Panel actual:** https://dfroot-modern-keys-zaid-density-reset.vercel.app

**Supabase Edge Function:** `dfroot-license` en el proyecto `zzlvupunploglgxbgllm`. Las tablas y las funciones de DFRoot utilizan el prefijo `dfroot_`; no se reutilizan los sistemas de keys de otros proyectos.

| Tipo | Prefijo | Vigencia |
| --- | --- | --- |
| Temporal | `DFM-XXXXXXXX-XXXXXXXX-XXXXXXXX-XXXXXXXX` | Una hora desde la primera activación; activable hasta 7 días después de la creación |
| Permanente | `DFP-XXXXXXXX-XXXXXXXX-XXXXXXXX-XXXXXXXX` | Sin fecha de vencimiento; revocable desde el panel |

Ambos tipos se vinculan a un identificador de instalación; solamente la primera activación realiza la vinculación. El servidor comprueba esa vinculación y la revocación **cada vez** que el usuario solicita iniciar el proceso desde la APK. Sin conexión o con servidor inaccesible no se autoriza la ejecución. No es una protección imposible de modificar mediante ingeniería inversa.

Las keys se generan con un origen aleatorio seguro, y **solo se almacenan hashes SHA-256 y los últimos 4 caracteres**. La clave completa solo se muestra al generarla. La función limita intentos repetidos y emplea una transacción con bloqueo de fila para impedir activaciones concurrentes desde varios dispositivos.

### Acceso al panel

El panel autentica con Supabase Auth y verifica que la cuenta esté incluida en `public.dfroot_admins`. Nadie obtiene privilegios administrativos solo por registrarse. Si necesitas autorizar una cuenta que ya exista en Supabase Auth:

```sql
insert into public.dfroot_admins(user_id)
select id from auth.users where lower(email) = lower('CORREO_AUTORIZADO@EJEMPLO.COM')
on conflict do nothing;
```

**No se deben incrustar claves de `service_role` en el panel o la APK**. El frontend contiene exclusivamente una clave pública de Supabase. El código de la interfaz de administración se versiona en `panel/index.html`.

## Compilar en GitHub

1. Abre [GitHub Actions](https://github.com/Fernan20881208/DFRoot-Modern/actions/workflows/build.yml) y selecciona la rama `modern-v4.5`.
2. Espera a que finalicen las ocho compilaciones de módulos GKI y el trabajo `Android APK`.
3. Descarga el artefacto `DFRoot-Modern-4.5` de una ejecución exitosa.
4. Extrae `DFRoot-Modern-4.5-debug.apk`. El ZIP incluye fuente y `SHA256SUMS.txt`.

La firma de depuración puede diferir de la versión oficial u otra ejecución de GitHub Actions, por lo que quizá necesites desinstalar otra aplicación `df.root` antes de instalarla. Esto borra sus ajustes. No desinstales el gestor KernelSU.

**Verificaciones CI:** identidad del motor v4.5 frente a la etiqueta oficial; compilación nativa ARM64; compilación APK y Android Lint; SDK mínimo 31; contenido ELF64 AArch64 (`libdfroot.so` y `libbootstrap.so`); integridad del artefacto.

El éxito de GitHub Actions **no prueba** que el exploit funcione en un teléfono particular. Para comprobar compatibilidad real se necesitan pruebas en un equipo autorizado y recuperable.
