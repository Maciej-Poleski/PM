package com.example.B;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class MyActivity extends Activity {

    boolean visible;
    private int runCount;
    private long startTime;
    private long timeOfInvisibility;
    private long invisibleSince;

    private void showStats() {
        TextView view = (TextView) findViewById(R.id.text_view);
        long currentTime = android.os.SystemClock.elapsedRealtime();
        long totalTime = currentTime - startTime;
        view.setText("Run count: " + runCount + "\nTotal time: " + (totalTime) / 1000 + "\nFront time: " + (totalTime - timeOfInvisibility) / 1000);

    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // run count
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        runCount = preferences.getInt("runCount", 0);

        if (savedInstanceState == null) {
            ++runCount;
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("runCount", runCount);
            editor.commit();

            //time since boot
            startTime = android.os.SystemClock.elapsedRealtime();

            //time of invisibility
            timeOfInvisibility = 0;
            invisibleSince = startTime; // jeszcze jest niewidoczny
            visible = false;
        } else {
            // wszystko jasne
            startTime = savedInstanceState.getLong("startTime");
            timeOfInvisibility = savedInstanceState.getLong("timeOfInvisibility");
            invisibleSince = savedInstanceState.getLong("invisibleSince");
            visible = savedInstanceState.getBoolean("visible");
        }

        new Timer(true).schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showStats();
                    }
                });
            }
        }, 0, 1000);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!visible) {
            long currentTime = android.os.SystemClock.elapsedRealtime();
            timeOfInvisibility += (currentTime - invisibleSince);
            visible = true;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        long currentTime = android.os.SystemClock.elapsedRealtime();
        invisibleSince = currentTime;
        visible = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("startTime", startTime);
        outState.putLong("timeOfInvisibility", timeOfInvisibility);
        outState.putLong("invisibleSince", invisibleSince);
        outState.putBoolean("visible", visible);
    }
}
