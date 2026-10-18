package df.root;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Switch;

/** Modern settings presentation; uses the same stored options as upstream v4.0. */
public class SettingsActivity extends Activity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_settings);
        ModernUi.applyInsets(this, findViewById(R.id.settingsRoot));
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnSource).setOnClickListener(v -> startActivity(new Intent(
                Intent.ACTION_VIEW, Uri.parse("https://github.com/Fernan20881208/DFRoot-Modern/tree/modern-v4.5"))));
        SharedPreferences prefs = createDeviceProtectedStorageContext().getSharedPreferences(
                ExploitRunner.PREFS_NAME, Context.MODE_PRIVATE);
        ComponentName receiver = new ComponentName(this, BootReceiver.class);
        Switch boot = findViewById(R.id.switchBoot);
        getPackageManager().setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        boot.setChecked(false);
        boot.setEnabled(false);
        // No automatic execution: hourly license requires online confirmation before every run.
        bindPreference(R.id.switchReboot, ExploitRunner.PREF_SOFT_REBOOT, prefs);
        bindPreference(R.id.switchModules, "disable_modules", prefs);
    }
    private void bindPreference(int id, String key, SharedPreferences prefs) {
        Switch toggle = findViewById(id);
        toggle.setChecked(prefs.getBoolean(key, false));
        toggle.setOnCheckedChangeListener((button, enabled) -> prefs.edit().putBoolean(key, enabled).apply());
    }
}
