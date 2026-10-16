package df.root;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Arrays;
import java.util.Locale;

/**
 * Remote-only Android TV diagnostics. No root engine, kernel patching or SU calls.
 */
public final class TvActivity extends Activity {
    private TextView status;
    private TextView device;
    private String diagnosticReport = "";
    private int diagnosticGeneration = 0;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv);
        status = findViewById(R.id.tvStatus);
        device = findViewById(R.id.tvDevice);
        Button copy = findViewById(R.id.tvCopy);
        Button refresh = findViewById(R.id.tvRefresh);
        Button source = findViewById(R.id.tvSource);
        copy.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) {
                cm.setPrimaryClip(ClipData.newPlainText("DFRoot TV diagnostic", diagnosticReport));
                Toast.makeText(this, R.string.tv_copied, Toast.LENGTH_SHORT).show();
            }
        });
        refresh.setOnClickListener(v -> updateDiagnostics());
        source.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "https://github.com/Fernan20881208/DFRoot-Modern/tree/android-tv-5.4")));
            } catch (ActivityNotFoundException ex) {
                Toast.makeText(this, R.string.tv_no_browser, Toast.LENGTH_LONG).show();
            }
        });
        updateDiagnostics();
        copy.requestFocus();
    }

    private void updateDiagnostics() {
        final int generation = ++diagnosticGeneration;
        final String kernel = System.getProperty("os.version", "desconocido");
        final boolean exactKernel = kernel.equals("5.4.284")
                || kernel.startsWith("5.4.284-") || kernel.startsWith("5.4.284+");
        final String visible = String.format(Locale.ROOT,
                "%s %s\nAndroid %s (API %d)\nKernel %s\nABI de las apps: %s\nHardware %s",
                Build.MANUFACTURER, Build.MODEL, Build.VERSION.RELEASE,
                Build.VERSION.SDK_INT, kernel, Arrays.toString(Build.SUPPORTED_ABIS),
                Build.HARDWARE);

        // Obtain live kernel flags in the background. SELinux may prevent reading
        // /proc/config.gz on some firmware versions; absence is not a failure.
        status.setText(R.string.tv_loading);
        device.setText(visible);
        new Thread(() -> {
            final KernelProbe.Result flags = KernelProbe.inspect();
            final String report = "DFRoot Modern Android TV — Solo diagnóstico\n"
                    + "Fabricante: " + Build.MANUFACTURER + "\n"
                    + "Modelo: " + Build.MODEL + "\n"
                    + "Hardware: " + Build.HARDWARE + "\n"
                    + "Placa: " + Build.BOARD + "\n"
                    + "Android: " + Build.VERSION.RELEASE
                    + " (API " + Build.VERSION.SDK_INT + ")\n"
                    + "Kernel: " + kernel + "\n"
                    + "ABI de las apps: " + Arrays.toString(Build.SUPPORTED_ABIS) + "\n"
                    + "Parche de seguridad: " + Build.VERSION.SECURITY_PATCH + "\n"
                    + "Paquete: " + getPackageName() + "\n\n"
                    + flags.configReport
                    + "Root: NO DISPONIBLE en esta edición de diagnóstico.\n";
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed() || generation != diagnosticGeneration) return;
                diagnosticReport = report;
                device.setText(visible + "\n\n" + flags.summary);
                status.setText(exactKernel ?
                        R.string.tv_kernel_identified : R.string.tv_kernel_other);
            });
        }, "DFRoot-ReadOnly-KernelProbe").start();
    }
}
