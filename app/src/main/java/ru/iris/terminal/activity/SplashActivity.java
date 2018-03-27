package ru.iris.terminal.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.iris.models.database.Device;
import ru.iris.terminal.R;
import ru.iris.terminal.httpapi.DeviceService;

public class SplashActivity extends Activity {
    private ObjectMapper objectMapper = new ObjectMapper();
    private static SharedPreferences preferences;

    public static SharedPreferences getPreferences() {
        return preferences;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);


        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        startHeavyProcessing();
    }

    private void startHeavyProcessing(){
        new StartupOperation().execute();
    }

    private class StartupOperation extends AsyncTask<Void, Void, List<DeviceService.DeviceIdent>> {
        private DeviceService deviceService;

        @Override
        protected List<DeviceService.DeviceIdent> doInBackground(Void... params) {
            Log.d(MainActivity.TAG, "Starting up");
            List<DeviceService.DeviceIdent> devices = new ArrayList<>();
            Log.d(MainActivity.TAG, "Prepare to load devices");

            DeviceService.destroy();
            deviceService = DeviceService.getInstance();

            Log.d(MainActivity.TAG, "DeviceService has been initiated successfully");

            try {
                Log.d(MainActivity.TAG, "Starting load devices");

                for(Device device : deviceService.getDevices(null, null)) {
                    DeviceService.DeviceIdent ident = new DeviceService.DeviceIdent(device.getSource(), device.getChannel(), device.getType());
                    devices.add(ident);
                }

                Log.d(MainActivity.TAG, "Loaded " + devices.size() + " devices");
            } catch (IOException e) {
                Log.d(MainActivity.TAG, "ERROR: " + e.getMessage());
            }

            return devices;
        }

        @Override
        protected void onPostExecute(List<DeviceService.DeviceIdent> result) {
            Log.d(MainActivity.TAG, "Started up");

            if(result == null) {
                Log.d(MainActivity.TAG, "No devices loaded - open preferences");
                Intent i = new Intent(SplashActivity.this, PreferencesActivity.class);
                startActivity(i);
                finish();
                return;
            }

            Intent i = new Intent(SplashActivity.this, MainActivity.class);
            try {
                i.putExtra("devices", objectMapper.writeValueAsString(result));
            } catch (JsonProcessingException e) {
                Log.d(MainActivity.TAG, "SERIALIZATION ERROR: " + e.getMessage());
            }
            startActivity(i);
            finish();
        }
    }
}