package df.root;

import android.app.Activity;
import android.content.ComponentName;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** UI-only redesign of the upstream DFRoot MainActivity. The exploit is unchanged. */
public class MainActivity extends Activity implements RunSession.Observer {
    static { System.loadLibrary("dfroot_archprobe"); }
    private static native String nativeArchitecture();


    private Button btnRun;
    private Button btnCopy;
    private Button btnClear;
    private ScrollView outputScroll;
    private TextView outputView;
    private TextView statusTitle;
    private TextView statusDetail;
    private TextView deviceModel;
    private TextView deviceAndroid;
    private TextView deviceKernel;
    private TextView devicePatch;
    private ProgressBar progressRun;
    private Spinner spinnerSuManager;
    private Context mDeCtx;
    private final RunSession session = RunSession.INSTANCE;
    private int mValidSuManagerPos = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Auto-boot must never bypass the online hourly license check.
        getPackageManager().setComponentEnabledSetting(new ComponentName(this, BootReceiver.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        mDeCtx = createDeviceProtectedStorageContext();
        setContentView(R.layout.activity_main);

        ModernUi.applyInsets(this, findViewById(R.id.rootLayout));
        btnRun = findViewById(R.id.btnRun);
        btnCopy = findViewById(R.id.btnCopy);
        btnClear = findViewById(R.id.btnClear);
        outputScroll = findViewById(R.id.outputScroll);
        outputView = findViewById(R.id.outputView);
        spinnerSuManager = findViewById(R.id.spinnerSuManager);
        progressRun = findViewById(R.id.progressRun);
        statusTitle = findViewById(R.id.statusTitle);
        statusDetail = findViewById(R.id.statusDetail);
        deviceModel = findViewById(R.id.deviceModel);
        deviceAndroid = findViewById(R.id.deviceAndroid);
        deviceKernel = findViewById(R.id.deviceKernel);
        devicePatch = findViewById(R.id.devicePatch);

        deviceModel.setText(Build.MANUFACTURER + " " + Build.MODEL + " • " + nativeArchitecture());
        deviceAndroid.setText("Android " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")");
        deviceKernel.setText(System.getProperty("os.version", "No disponible"));
        devicePatch.setText(Build.VERSION.SECURITY_PATCH);

        btnRun.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle(R.string.arm32_unavailable_title)
                .setMessage(R.string.arm32_unavailable_detail)
                .setPositiveButton(R.string.back, null).show());
        btnCopy.setOnClickListener(v -> {
            String logs = outputView.getText().toString();
            if (logs.isEmpty()) {
                Toast.makeText(this, R.string.log_empty, Toast.LENGTH_SHORT).show();
                return;
            }
            ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (cm != null) {
                cm.setPrimaryClip(ClipData.newPlainText("DFRoot log", logs));
                Toast.makeText(this, R.string.log_copied, Toast.LENGTH_SHORT).show();
            }
        });
        btnClear.setOnClickListener(v -> session.clear());
        findViewById(R.id.btnSettings).setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        findViewById(R.id.btnRefresh).setOnClickListener(v -> populateManagers());
        session.attach(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (btnRun != null) populateManagers();
    }

    private boolean hasDfMarker() {
        return new File("/dev/df").exists();
    }

    private void refreshStatus() {
        statusTitle.setText(R.string.arm32_unavailable_title);
        statusDetail.setText(R.string.arm32_unavailable_detail);
        btnRun.setText(R.string.arm32_action_unavailable);
        btnRun.setEnabled(false);
        spinnerSuManager.setEnabled(false);
        progressRun.setVisibility(View.GONE);
        findViewById(R.id.btnRefresh).setEnabled(true);
    }

    @Override public void onSessionChanged() {
        String logs = session.logs();
        if (!logs.contentEquals(outputView.getText())) {
            boolean atBottom = !outputScroll.canScrollVertically(1);
            outputView.setText(logs);
            if (atBottom) outputScroll.post(() -> outputScroll.fullScroll(View.FOCUS_DOWN));
        }
        refreshStatus();
    }

    @Override protected void onDestroy() {
        session.detach(this);
        super.onDestroy();
    }

    private static boolean hasKsud(ApplicationInfo info) {
        // Match v4.0's nativeLibraryDir contract; hasKsud() was added upstream later.
        return info.nativeLibraryDir != null && new File(info.nativeLibraryDir, "libksud.so").isFile();
    }

    private void populateManagers() {
        mValidSuManagerPos = 0;
        spinnerSuManager.setOnItemSelectedListener(null);
        PackageManager pm = getPackageManager();
        List<SuManagerEntry> entries = new ArrayList<>();
        for (ApplicationInfo ai : pm.getInstalledApplications(0)) {
            if ((ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0) continue;
            if (!hasKsud(ai)) continue;
            entries.add(new SuManagerEntry(ai.packageName,
                    pm.getApplicationLabel(ai), pm.getApplicationIcon(ai)));
        }
        entries.sort((a, b) -> a.label.toString().compareToIgnoreCase(b.label.toString()));
        entries.add(0, new SuManagerEntry(null, getString(R.string.choose_manager), null));
        spinnerSuManager.setAdapter(new SuManagerAdapter(this, entries));

        SharedPreferences prefs = mDeCtx.getSharedPreferences(ExploitRunner.PREFS_NAME,
                Context.MODE_PRIVATE);
        String saved = prefs.getString(ExploitRunner.PREF_SU_MANAGER, null);
        boolean savedFound = false;
        for (int i = 1; i < entries.size(); i++) {
            if (entries.get(i).packageName.equals(saved)) {
                spinnerSuManager.setSelection(i);
                mValidSuManagerPos = i;
                savedFound = true;
                break;
            }
        }
        if (!savedFound && saved != null) {
            prefs.edit().remove(ExploitRunner.PREF_SU_MANAGER).apply();
        }

        spinnerSuManager.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                SuManagerEntry selected = entries.get(pos);
                mValidSuManagerPos = selected.packageName == null ? 0 : pos;
                if (selected.packageName == null) prefs.edit().remove(ExploitRunner.PREF_SU_MANAGER).apply();
                if (selected.packageName != null) {
                    prefs.edit().putString(ExploitRunner.PREF_SU_MANAGER,
                            selected.packageName).apply();
                }
                refreshStatus();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {
                mValidSuManagerPos = 0;
                refreshStatus();
            }
        });

        ((TextView) findViewById(R.id.managerHelp)).setText(entries.size() == 1 ? R.string.no_managers : R.string.manager_hint);
        refreshStatus();
    }

    private static class SuManagerEntry {
        final String packageName;
        final CharSequence label;
        final Drawable icon;

        SuManagerEntry(String pkg, CharSequence label, Drawable icon) {
            this.packageName = pkg;
            this.label = label;
            this.icon = icon;
        }
    }

    private static class SuManagerAdapter extends ArrayAdapter<SuManagerEntry> {
        SuManagerAdapter(Context ctx, List<SuManagerEntry> items) {
            super(ctx, R.layout.item_su_manager, items);
        }

        @Override
        public View getView(int pos, View v, ViewGroup parent) {
            if (v == null || v.getTag() != Boolean.FALSE)
                v = LayoutInflater.from(getContext()).inflate(R.layout.item_su_manager_closed,
                        parent, false);
            v.setTag(Boolean.FALSE);
            return bindClosedView(pos, v);
        }

        @Override
        public View getDropDownView(int pos, View v, ViewGroup parent) {
            if (v == null || v.getTag() != Boolean.TRUE)
                v = LayoutInflater.from(getContext()).inflate(R.layout.item_su_manager,
                        parent, false);
            v.setTag(Boolean.TRUE);
            return bindView(pos, v);
        }

        @Override public boolean isEnabled(int pos) {
            return true;
        }

        private View bindClosedView(int pos, View v) {
            SuManagerEntry e = getItem(pos);
            ImageView icon = v.findViewById(R.id.iconApp);
            TextView label = v.findViewById(R.id.labelApp);
            if (e.packageName == null) {
                icon.setVisibility(View.GONE);
                label.setVisibility(View.VISIBLE);
                label.setText(e.label);
            } else {
                icon.setVisibility(View.VISIBLE);
                label.setVisibility(View.VISIBLE);
                label.setText(e.label);
                icon.setImageDrawable(e.icon);
            }
            return v;
        }

        private View bindView(int pos, View v) {
            SuManagerEntry e = getItem(pos);
            ImageView icon = v.findViewById(R.id.iconApp);
            if (e.icon == null) icon.setVisibility(View.GONE);
            else {
                icon.setVisibility(View.VISIBLE);
                icon.setImageDrawable(e.icon);
            }
            ((TextView) v.findViewById(R.id.labelApp)).setText(e.label);
            return v;
        }
    }
}
