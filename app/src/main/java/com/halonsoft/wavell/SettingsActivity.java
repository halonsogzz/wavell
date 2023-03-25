package com.halonsoft.wavell;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;


public class SettingsActivity extends AppCompatActivity {
    private CameraManager cameraManager;
    private String getCameraID;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    TextView text, text2, text3;
    long starttime = 0;

    final Handler h = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            long millis = System.currentTimeMillis() - starttime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            // text.setText(String.format("%d:%02d", minutes, seconds));

            Log.d("handleMessage", String.format("%d:%02d", minutes, seconds));
            return false;
        }
    });

    //runs without timer be reposting self
    Handler h2 = new Handler();
    Runnable run = new Runnable() {

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - starttime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            //text3.setText(String.format("%d:%02d", minutes, seconds));
            Log.d("run", String.format("%d:%02d", minutes, seconds));

            try {
                cameraManager.setTorchMode(getCameraID, (seconds & 1) == 0);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

            h2.postDelayed(this, 500);
        }
    };

    class firstTask extends TimerTask {
        @Override
        public void run() {
            h.sendEmptyMessage(0);
        }
    }

    ;

    class secondTask extends TimerTask {

        @Override
        public void run() {
            SettingsActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    long millis = System.currentTimeMillis() - starttime;
                    int seconds = (int) (millis / 1000);
                    int minutes = seconds / 60;
                    seconds = seconds % 60;

                    // text2.setText(String.format("%d:%02d", minutes, seconds));
                    Log.d("run", String.format("%d:%02d", minutes, seconds));

                }
            });
        }
    }

    ;

    @Override
    public void onPause() {
        super.onPause();
        timer.cancel();
        timer.purge();
        h2.removeCallbacks(run);
        //   Button b = (Button)findViewById(R.id.button);
        //b.setText("start");
        Log.d("Test", "start");
    }

    Timer timer = new Timer();

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            getCameraID = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            //cameraManager.setTorchMode(getCameraID, true);
        }
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                // Implementation
                //Log.d("Test", key);

                if (key.equals("sync")) {
                    timer.cancel();
                    timer.purge();
                    h2.removeCallbacks(run);
                    //b.setText("start");
                    Log.d("Test", "start");
                } else {
                    starttime = System.currentTimeMillis();
                    timer = new Timer();
                    timer.schedule(new firstTask(), 0, 500);
                    timer.schedule(new secondTask(), 0, 500);
                    h2.postDelayed(run, 0);
                    //b.setText("stop");
                    Log.d("Test", "stop");
                }
                try {
                    Log.d("Test", key);
                    //Log.d("Test", p);
                    cameraManager.setTorchMode(getCameraID, false);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }


            }
        };
        prefs.registerOnSharedPreferenceChangeListener(listener);


    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }

}