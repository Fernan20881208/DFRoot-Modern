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
        String kernel = System.getProperty("os.version", "desconocido");
        boolean exactKernel = kernel.equals("5.4.284") || kernel.startsWith("5.4.284-")
                || kernel.startsWith("5.4.284+");
        diagnosticReport = "DFRoot Modern Android TV — Solo diagnóstico\\n"
                + "Fabricante: " + Build.MANUFACTURER + "\\n"
                + "Modelo: " + Build.MODEL + "\\n"
                + "Hardware: " + Build.HARDWARE + "\\n"
                + "Placa: " + Build.BOARD + "\\n"
                + "Android: " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")\\n"
                + "Kernel: " + kernel + "\\n"
                + "ABIs: " + Arrays.toString(Build.SUPPORTED_ABIS) + "\\n"
                + "Parche de seguridad: " + Build.VERSION.SECURITY_PATCH + "\\n"
                + "Paquete: " + getPackageName() + "\\n"
                + "Root: NO DISPONIBLE en esta edicion de interfaz.\\n";
        String visible = String.format(Locale.ROOT,
                "%s %s\\nAndroid %s (API %d)\\nKernel %s\\nArquitecturas %s\\nHardware %s",
                Build.MANUFACTURER, Build.MODEL, Build.VERSION.RELEASE,
                Build.VERSION.SDK_INT, kernel, Arrays.toString(Build.SUPPORTED_ABIS), Build.HARDWARE);
        device.setText(visible);
        status.setText(exactKernel
            ? getString(R.string.tv_kernel_identified)
            : getString(R.string.tv_kernel_other));
    }
}
