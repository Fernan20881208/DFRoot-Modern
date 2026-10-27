# DFRoot Modern 4.7 — ARM64

[![Build ARM64 4.7](https://github.com/Fernan20881208/DFRoot-Modern/actions/workflows/build.yml/badge.svg?branch=modern-v4.7)](https://github.com/Fernan20881208/DFRoot-Modern/actions/workflows/build.yml)

Aplicación Android con interfaz Material You, basada en el motor de **[DFRoot oficial v4.7](https://github.com/diabl0w/DFRoot/releases/tag/v4.7)**, de diabl0w, y control de licencias remoto mediante Supabase. La rama de esta edición es **`modern-v4.7`**.

Esta versión es exclusivamente ARM64 (`arm64-v8a`). Las ramas `modern-v4.0`, `modern-v4.5`, `arm32-ui` y `android-tv-5.4` se mantienen separadas.

## Motor v4.7

Los archivos del motor que cambiaron en la versión oficial 4.7 se sincronizan con la etiqueta upstream `v4.7`, incluidos `app/src/main/jni/`, `lkm/` y `app/src/main/java/df/root/ExploitRunner.java`. La compilación compara esos archivos con el código oficial antes de empaquetar la APK.

- `applicationId = "df.root"` para conservar las rutas internas del motor.
- `buildFeatures.buildConfig = true`, requerido por `ExploitRunner`.
- Arquitectura: ARM64. Android mínimo: **Android 12L (API 32)**, según los requisitos de upstream v4.7. No se instala en Android 12/API 31.
- El inicio automático queda deshabilitado. Antes de cada ejecución, la APK valida la licencia online con Supabase.
- La compilación no garantiza que el teléfono sea vulnerable ni que el motor funcione en un firmware concreto.

## Licencias y panel de administración

**Panel:** https://dfroot-modern-keys-zaid-density-reset.vercel.app

**Función de Supabase:** `dfroot-license`, en el proyecto `zzlvupunploglgxbgllm`. El panel usa Supabase Auth y verifica la lista `public.dfroot_admins`; registrarse por sí solo no concede permisos de administrador.

| Tipo | Prefijo | Vigencia |
| --- | --- | --- |
| Temporal | `DFM-XXXXXXXX-XXXXXXXX-XXXXXXXX-XXXXXXXX` | Una hora desde la primera activación; activable hasta 7 días después de crearla |
| Permanente | `DFP-XXXXXXXX-XXXXXXXX-XXXXXXXX-XXXXXXXX` | Sin vencimiento; puede revocarse desde el panel |

Ambas licencias se vinculan a una instalación y el servidor comprueba la vinculación y la revocación cada vez que la APK solicita ejecutar el proceso. Sin conexión o si el servidor no puede validar la clave, no se autoriza la ejecución. Las claves completas no se almacenan en la tabla: solo hashes SHA-256 y los últimos cuatro caracteres.

Para dar acceso de administrador a una cuenta que ya existe en Supabase Auth:

```sql
insert into public.dfroot_admins(user_id)
select id
from auth.users
where lower(email) = lower('CORREO_AUTORIZADO@EJEMPLO.COM')
on conflict do nothing;
```

No incrustes claves `service_role` en la APK ni en el panel. El frontend solo debe usar la clave pública de Supabase.

## Compilación

1. Abre [GitHub Actions](https://github.com/Fernan20881208/DFRoot-Modern/actions/workflows/build.yml) y selecciona la rama `modern-v4.7`.
2. Espera a que terminen las ocho compilaciones del módulo de kernel y el trabajo `Android APK`.
3. Descarga el artefacto `DFRoot-Modern-4.7`.
4. El artefacto contiene `DFRoot-Modern-4.7-debug.apk`, el ZIP de fuentes y `SHA256SUMS.txt`.

La APK de CI usa firma de depuración; puede ser necesario desinstalar otra instalación de `df.root` firmada con una clave diferente. Desinstalar la aplicación borra sus ajustes. No desinstales el gestor KernelSU.

**Verificaciones CI:** motor nativo comparado con upstream `v4.7`; compilación ARM64; compilación APK y Android Lint; SDK mínimo 32; ejecutables ELF64/AArch64; integridad del artefacto. El éxito de CI no sustituye las pruebas reales en un dispositivo recuperable y autorizado.


<!-- CI diagnosis branch: no application code changes. -->
