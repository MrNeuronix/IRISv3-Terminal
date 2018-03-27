package ru.iris.terminal.fragments;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.concurrent.ExecutionException;

import ru.iris.models.bus.devices.DeviceChangeEvent;
import ru.iris.models.database.Device;
import ru.iris.models.database.DeviceValue;
import ru.iris.models.protocol.data.DataLevel;
import ru.iris.terminal.R;
import ru.iris.terminal.httpapi.DeviceService;
import ru.iris.terminal.tasks.GetDeviceOperation;

import static ru.iris.terminal.activity.MainActivity.TAG;

public class DoorSensorFragment implements FragmentHandler {
    private DeviceService.DeviceIdent deviceIdent;
    private ImageView icon;
    private TextView nameLabel;

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
            DeviceValue status = device.getValues().get("status");

            if(status != null && status.getCurrentValue() != null) {
                if(status.getCurrentValue().equals("closed")) {
                    icon.setBackgroundResource(R.drawable.doorclosed);
                } else {
                    icon.setBackgroundResource(R.drawable.dooropen);
                }
            }
        }
    }

    @Override
    public void stop() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public View handle(LayoutInflater inflater, ViewGroup container, DeviceService.DeviceIdent deviceIdent) {
        this.deviceIdent = deviceIdent;

        EventBus.getDefault().register(this);

        View view = inflater.inflate(R.layout.doorsensor, container, false);
        nameLabel = view.findViewById(R.id.name);
        icon = view.findViewById(R.id.door);

        updateData(deviceIdent);

        return view;
    }

    @Subscribe
    public void onMessageEvent(DeviceChangeEvent event) {
        if(event.getProtocol().equals(deviceIdent.getSource()) && event.getChannel().equals(deviceIdent.getChannel())) {
            Log.d(TAG, "Event come to door sensor fragment");
            DataLevel dataLevel = (DataLevel) event.getData();
            if(dataLevel.getTo().equals("false")) {
                icon.setBackgroundResource(R.drawable.doorclosed);
            } else {
                icon.setBackgroundResource(R.drawable.dooropen);
            }
        }
    }
}
