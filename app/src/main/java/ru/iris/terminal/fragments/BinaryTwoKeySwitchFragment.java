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
import ru.iris.models.protocol.data.DataSubChannelLevel;
import ru.iris.terminal.R;
import ru.iris.terminal.activity.MainActivity;
import ru.iris.terminal.httpapi.DeviceService;

import static ru.iris.terminal.activity.MainActivity.TAG;

public class BinaryTwoKeySwitchFragment implements FragmentHandler {
    private boolean enabled1 = false;
    private boolean enabled2 = false;
    private Device device;
    private ImageButton lamp1;
    private ImageButton lamp2;
    private final DeviceService deviceService = DeviceService.getInstance();

    @Override
    public View handle(LayoutInflater inflater, ViewGroup container, Device device) {
        this.device = device;

        EventBus.getDefault().register(this);

        View view = inflater.inflate(R.layout.binarytwokeyswitch, container, false);
        TextView nameLabel = view.findViewById(R.id.name);
        lamp1 = view.findViewById(R.id.toggle1);
        lamp2 = view.findViewById(R.id.toggle2);

        lamp1.setOnClickListener(listener);
        lamp2.setOnClickListener(listener2);

        nameLabel.setText(device.getHumanReadable());

        DeviceValue level1 = device.getValues().get("level1");
        DeviceValue level2 = device.getValues().get("level2");

        if(level1 != null && level1.getCurrentValue() != null) {
            if(level1.getCurrentValue().equals("255")) {
                enabled1 = true;
                lamp1.setBackgroundResource(R.drawable.lampon);
            } else {
                enabled1 = false;
                lamp1.setBackgroundResource(R.drawable.lampoff);
            }
        }

        if(level2 != null && level2.getCurrentValue() != null) {
            if(level2.getCurrentValue().equals("255")) {
                enabled2 = true;
                lamp2.setBackgroundResource(R.drawable.lampon);
            } else {
                enabled2 = false;
                lamp2.setBackgroundResource(R.drawable.lampoff);
            }
        }

        return view;
    }

    @Subscribe
    public void onMessageEvent(Event event) {
        Log.d(TAG, "Event come to binary two key switch fragment");

        if(event instanceof DeviceChangeEvent) {
            DeviceChangeEvent e = (DeviceChangeEvent) event;

            if(e.getProtocol().equals(device.getSource()) && e.getChannel().equals(device.getChannel()) && e.getData() instanceof DataSubChannelLevel) {
                DataSubChannelLevel dataLevel = (DataSubChannelLevel) e.getData();
                if(dataLevel.getSubChannel() == 1) {
                    if (dataLevel.getTo().equals("255")) {
                        enabled1 = true;
                        lamp1.setBackgroundResource(R.drawable.lampon);
                    } else {
                        enabled1 = false;
                        lamp1.setBackgroundResource(R.drawable.lampoff);
                    }
                } else {
                    if (dataLevel.getTo().equals("255")) {
                        enabled2 = true;
                        lamp2.setBackgroundResource(R.drawable.lampon);
                    } else {
                        enabled2 = false;
                        lamp2.setBackgroundResource(R.drawable.lampoff);
                    }
                }
            }
        }
    }

    private ImageButton.OnClickListener listener = new ImageButton.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            Log.d(TAG, "CLICK 1!");
            try {
                if(enabled1) {
                    new DeviceOperation().execute("0", "1").get();
                    enabled1 = false;
                    lamp1.setBackgroundResource(R.drawable.lampoff);
                } else {
                    new DeviceOperation().execute("255", "1").get();
                    enabled1 = true;
                    lamp1.setBackgroundResource(R.drawable.lampon);
                }
            } catch (InterruptedException | ExecutionException e) {
                Log.d(TAG, "ERROR: " + e.getMessage());
            }
        }
    };

    private ImageButton.OnClickListener listener2 = new ImageButton.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            Log.d(TAG, "CLICK 2!");
            try {
                if(enabled2) {
                    new DeviceOperation().execute("0", "2").get();
                    enabled2 = false;
                    lamp2.setBackgroundResource(R.drawable.lampoff);
                } else {
                    new DeviceOperation().execute("255", "2").get();
                    enabled2 = true;
                    lamp2.setBackgroundResource(R.drawable.lampon);
                }
            } catch (InterruptedException | ExecutionException e) {
                Log.d(TAG, "ERROR: " + e.getMessage());
            }
        }
    };

    private class DeviceOperation extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            Log.d(MainActivity.TAG, "Set level " + params[0] + " to device " + device.getHumanReadable() + ", subchannel: " + params[1]);

            try {
                deviceService.setDeviceLevel(device.getSource(), device.getChannel(), Integer.valueOf(params[1]), params[0]);
            } catch (IOException e) {
                Log.d(TAG, "ERROR: " + e.getMessage());
            }

            return null;
        }
    }

}
