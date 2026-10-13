package df.root;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

final class LicenseGate {
    private static final String ENDPOINT = "https://zzlvupunploglgxbgllm.supabase.co/functions/v1/dfroot-license";
    private LicenseGate() {}
    static void request(Activity activity, Runnable approved) {
        SharedPreferences prefs = activity.getSharedPreferences("dfroot_license", Context.MODE_PRIVATE);
        EditText input = new EditText(activity);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        input.setText(prefs.getString("key", ""));
        input.setSelectAllOnFocus(true);
        int pad = (int)(20 * activity.getResources().getDisplayMetrics().density);
        android.widget.LinearLayout container = new android.widget.LinearLayout(activity);
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
        container.setPadding(pad, pad/2, pad, 0);
        container.addView(input);
        new AlertDialog.Builder(activity)
            .setTitle(R.string.license_prompt)
            .setMessage(R.string.license_hint)
            .setView(container)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.execute, (dialog, which)-> {
                String key = input.getText().toString().trim().toUpperCase(java.util.Locale.ROOT);
                if (!key.matches("DFM-([A-F0-9]{8}-){3}[A-F0-9]{8}")) {
                    Toast.makeText(activity,R.string.license_invalid,Toast.LENGTH_LONG).show();
                    return;
                }
                String device = prefs.getString("device",null);
                if (device == null) {
                    device = UUID.randomUUID().toString();
                    prefs.edit().putString("device",device).apply();
                }
                final String deviceId = device;
                Toast.makeText(activity,R.string.license_checking,Toast.LENGTH_SHORT).show();
                new Thread(()->{
                    boolean valid=false;
                    try {
                        HttpURLConnection conn=(HttpURLConnection)new URL(ENDPOINT).openConnection();
                        try {
                            conn.setConnectTimeout(8000);
                            conn.setReadTimeout(8000);
                            conn.setRequestMethod("POST");
                            conn.setDoOutput(true);
                            conn.setRequestProperty("Content-Type","application/json");
                            JSONObject body=new JSONObject();
                            body.put("action","redeem");body.put("key",key);body.put("device",deviceId);
                            try(OutputStream stream=conn.getOutputStream()){stream.write(body.toString().getBytes(StandardCharsets.UTF_8));}
                            if(conn.getResponseCode()==200) {
                                byte[] response=conn.getInputStream().readAllBytes();
                                valid=new JSONObject(new String(response, StandardCharsets.UTF_8)).optBoolean("valid",false);
                            }
                        } finally {conn.disconnect();}
                    }catch(Exception ignored){}
                    final boolean ok=valid;
                    activity.runOnUiThread(()->{
                        if(activity.isFinishing()||activity.isDestroyed())return;
                        if(ok){prefs.edit().putString("key",key).apply();approved.run();}
                        else Toast.makeText(activity,R.string.license_invalid,Toast.LENGTH_LONG).show();
                    });
                },"DFRoot-License-Check").start();
            }).show();
    }
}