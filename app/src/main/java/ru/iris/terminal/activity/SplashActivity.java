package ru.iris.terminal.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

import ru.iris.models.database.Device;
import ru.iris.terminal.R;
import ru.iris.terminal.httpapi.DeviceService;

public class SplashActivity extends Activity {
    private Activity activity;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.activity = this;
        setContentView(R.layout.loading);
        startHeavyProcessing();
    }

    private void startHeavyProcessing(){
        new StartupOperation().execute("");
    }

    private class StartupOperation extends AsyncTask<String, Void, List<Device>> {
        private DeviceService deviceService;

        @Override
        protected List<Device> doInBackground(String... params) {
            Log.d(MainActivity.TAG, "Starting up");

            List<Device> devices = null;

            SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("server", "http://192.168.10.58:9000/api/");
            editor.apply();

            Log.d(MainActivity.TAG, "Prepare to load devices");

            deviceService = new DeviceService(activity);

            Log.d(MainActivity.TAG, "DeviceService has been initiated successfully");

            try {
                Log.d(MainActivity.TAG, "Starting load devices");
                devices = deviceService.getDevices(null, null);
                Log.d(MainActivity.TAG, "Devices loaded");
            } catch (IOException e) {
                Log.d(MainActivity.TAG, "ERROR: " + e.getMessage());
            }

            return devices;
        }

        @Override
        protected void onPostExecute(List<Device> result) {
            Log.d(MainActivity.TAG, "Started up");
            Intent i = new Intent(SplashActivity.this, MainActivity.class);
            try {
                i.putExtra("devices", objectMapper.writeValueAsString(result));
            } catch (JsonProcessingException e) {
                Log.d(MainActivity.TAG, "SERIALIZATION ERROR: " + e.getMessage());
            }
            startActivity(i);
            finish();
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }
}