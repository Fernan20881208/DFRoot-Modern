package df.root;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

/**
 * Reads only a small allowlist of flags from the currently running Linux kernel.
 * Does not invoke su, insmod, privileged services, or write to any device files.
 */
final class KernelProbe {
    private static final String CONFIG_PATH = "/proc/config.gz";
    private static final Set<String> FLAGS = new LinkedHashSet<>(Arrays.asList(
            "CONFIG_ARM64",
            "CONFIG_ARCH_RTK6748",
            "CONFIG_MODULES",
            "CONFIG_MODVERSIONS",
            "CONFIG_MODULE_UNLOAD",
            "CONFIG_MODULE_SIG",
            "CONFIG_MODULE_SIG_FORCE",
            "CONFIG_IKCONFIG_PROC",
            "CONFIG_KALLSYMS",
            "CONFIG_KALLSYMS_ALL",
            "CONFIG_ARM64_4K_PAGES",
            "CONFIG_ARM64_PAGE_SHIFT",
            "CONFIG_LOCALVERSION"
    ));

    static final class Result {
        final String summary;
        final String configReport;
        Result(String summary, String configReport) {
            this.summary = summary;
            this.configReport = configReport;
        }
    }

    private KernelProbe() {}

    static Result inspect() {
        Map<String, String> detected = new LinkedHashMap<>();
        String unavailable = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new GZIPInputStream(new FileInputStream(CONFIG_PATH)),
                StandardCharsets.UTF_8))) {
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null && count++ < 200000) {
                if (line.startsWith("CONFIG_")) {
                    int eq = line.indexOf('=');
                    if (eq > 0) {
                        String name = line.substring(0, eq);
                        if (FLAGS.contains(name)) detected.put(name, line.substring(eq + 1));
                    }
                } else if (line.startsWith("# CONFIG_") && line.endsWith(" is not set")) {
                    String name = line.substring(2, line.length() - " is not set".length());
                    if (FLAGS.contains(name)) detected.put(name, "n");
                }
            }
        } catch (IOException | SecurityException e) {
            unavailable = e.getClass().getSimpleName();
        }

        if (unavailable != null || detected.isEmpty()) {
            String note = unavailable == null ? "No se encontraron indicadores" :
                    "No se pudo abrir /proc/config.gz (" + unavailable + ")";
            return new Result(
                    "Arquitectura del kernel: sin confirmar desde esta APK\n" + note,
                    note + "\nNo se interpreta la ausencia de acceso como incompatibilidad.\n");
        }

        String arch = "y".equals(detected.get("CONFIG_ARM64")) ?
                "ARM64 (AArch64)" : "sin confirmar";
        String modules = "y".equals(detected.get("CONFIG_MODULES")) ?
                "habilitados en el kernel; carga no autorizada" : "no confirmados";
        String versions = "y".equals(detected.get("CONFIG_MODVERSIONS")) ?
                "activadas" : "no confirmadas";
        String soc = "y".equals(detected.get("CONFIG_ARCH_RTK6748")) ?
                "RTD6748 confirmado por config" : "RTD6748 sin confirmar por config";
        String summary = "Kernel: " + arch + "\n"
                + "SoC: " + soc + "\n"
                + "Módulos: " + modules + "\n"
                + "CRC de símbolos: " + versions + "\n"
                + "Root por DFRoot: NO DISPONIBLE (kernel 5.4.284 sin port)";

        StringBuilder report = new StringBuilder(
                "===== Configuración ACTIVA del kernel (/proc/config.gz) =====\n");
        for (String name : FLAGS) {
            report.append(name).append('=').append(detected.getOrDefault(name, "sin dato")).append('\n');
        }
        report.append("Nota: CONFIG_MODULES=y no concede permisos para cargar módulos.\n")
              .append("La fuente pública TCL 5.4.210 NO coincide con el kernel 5.4.284+.\n");
        return new Result(summary, report.toString());
    }
}
