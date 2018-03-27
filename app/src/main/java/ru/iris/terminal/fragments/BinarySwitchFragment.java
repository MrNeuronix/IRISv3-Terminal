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
import ru.iris.terminal.tasks.GetDeviceOperation;

import static ru.iris.terminal.activity.MainActivity.TAG;

public class BinarySwitchFragment implements FragmentHandler {
    private boolean enabled = false;
    private DeviceService.DeviceIdent deviceIdent;
    private ImageButton lamp;
    private TextView nameLabel;
    private final DeviceService deviceService = DeviceService.getInstance();

    @Override
    public View handle(LayoutInflater inflater, ViewGroup container, DeviceService.DeviceIdent deviceIdent) {
        this.deviceIdent = deviceIdent;

        EventBus.getDefault().register(this);

        View view = inflater.inflate(R.layout.binaryswitch, container, false);
        nameLabel = view.findViewById(R.id.name);
        lamp = view.findViewById(R.id.toggle);
        lamp.setOnClickListener(listener);

        updateDevice(deviceIdent);

        return view;
    }

    @Override
    public void stop() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void updateData(DeviceService.DeviceIdent deviceIdent) {
        updateDevice(deviceIdent);
    }

    private void updateDevice(DeviceService.DeviceIdent deviceIdent) {
        this.deviceIdent = deviceIdent;
        Device device = null;
        try {
            device = new GetDeviceOperation().execute(deviceIdent).get();
        } catch (InterruptedException | ExecutionException e) {
            Log.d(TAG, "ERROR: " + e.getMessage());
        }

        if(device != null) {
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
        }
    }

    @Subscribe
    public void onMessageEvent(DeviceChangeEvent e) {
        if(e.getProtocol().equals(deviceIdent.getSource()) && e.getChannel().equals(deviceIdent.getChannel())) {
            Log.d(TAG, "Event come to binary switch fragment");
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
            Log.d(MainActivity.TAG, "Set level " + params[0] + " to deviceIdent " + deviceIdent.getChannel());

            try {
                deviceService.setDeviceLevel(deviceIdent.getSource(), deviceIdent.getChannel(), null, params[0]);
            } catch (IOException e) {
                Log.d(TAG, "ERROR: " + e.getMessage());
            }

            return null;
        }
    }
}
