package ru.iris.terminal.fragments;

import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import ru.iris.models.bus.Event;
import ru.iris.models.bus.devices.DeviceChangeEvent;
import ru.iris.models.database.Device;
import ru.iris.models.database.DeviceValue;
import ru.iris.models.protocol.data.DataLevel;
import ru.iris.terminal.R;
import ru.iris.terminal.activity.MainActivity;
import ru.iris.terminal.httpapi.DeviceService;

import static ru.iris.terminal.activity.MainActivity.TAG;

public class DoorSensorFragment implements FragmentHandler {
    private boolean enabled = false;
    private Device device;
    private ImageButton lamp;
    private final DeviceService deviceService = DeviceService.getInstance();

    @Override
    public View handle(LayoutInflater inflater, ViewGroup container, Device device) {
        this.device = device;

        EventBus.getDefault().register(this);

        View view = inflater.inflate(R.layout.binaryswitch, container, false);
        TextView nameLabel = view.findViewById(R.id.name);
        lamp = view.findViewById(R.id.toggle);

        lamp.setOnClickListener(listener);

        nameLabel.setText(device.getHumanReadable());

        DeviceValue level = device.getValues().get("level");

        if(level != null && level.getCurrentValue() != null) {
            if(level.getCurrentValue().equals("255")) {
                enabled = true;
                lamp.setBackgroundResource(R.drawable.lampon);
            } else {
                enabled = false;
                lamp.setBackgroundResource(R.drawable.lampoff);
            }
        }

        return view;
    }

    @Subscribe
    public void onMessageEvent(Event event) {
        Log.d(TAG, "Event come to binary switch fragment");

        if(event instanceof DeviceChangeEvent) {
            DeviceChangeEvent e = (DeviceChangeEvent) event;

            if(e.getProtocol().equals(device.getSource()) && e.getChannel().equals(device.getChannel()) && e.getData() instanceof DataLevel) {
                DataLevel dataLevel = (DataLevel) e.getData();
                if(dataLevel.getTo().equals("255")) {
                    enabled = true;
                    lamp.setBackgroundResource(R.drawable.lampon);
                } else {
                    enabled = false;
                    lamp.setBackgroundResource(R.drawable.lampoff);
                }
            }
        }
    }

    private ImageButton.OnClickListener listener = new ImageButton.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            Log.d(TAG, "CLICK!");
            try {
                if(enabled) {
                    new DeviceOperation().execute("0").get();
                    enabled = false;
                    lamp.setBackgroundResource(R.drawable.lampoff);
                } else {
                    new DeviceOperation().execute("255").get();
                    enabled = true;
                    lamp.setBackgroundResource(R.drawable.lampon);
                }
            } catch (InterruptedException | ExecutionException e) {
                Log.d(TAG, "ERROR: " + e.getMessage());
            }
        }
    };

    private class DeviceOperation extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            Log.d(MainActivity.TAG, "Set level " + params[0] + " to device " + device.getHumanReadable());

            try {
                deviceService.setDeviceLevel(device.getSource(), device.getChannel(), null, params[0]);
            } catch (IOException e) {
                Log.d(TAG, "ERROR: " + e.getMessage());
            }

            return null;
        }
    }

}
