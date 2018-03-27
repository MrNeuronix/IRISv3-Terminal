package ru.iris.terminal.tasks;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import ru.iris.models.database.Device;
import ru.iris.terminal.activity.MainActivity;
import ru.iris.terminal.httpapi.DeviceService;

import static ru.iris.terminal.activity.MainActivity.TAG;

public class GetDeviceOperation extends AsyncTask<DeviceService.DeviceIdent, Void, Device> {
    @Override
    protected Device doInBackground(DeviceService.DeviceIdent... params) {
        Log.d(MainActivity.TAG, "Get device: " + params[0].getChannel());
        Device device = null;
        DeviceService deviceService = DeviceService.getInstance();
        try {
            device = deviceService.getDevices(params[0].getSource(), params[0].getChannel()).get(0);
        } catch (IOException e) {
            Log.d(TAG, "ERROR: " + e.getMessage());
        }

        return device;
    }
}