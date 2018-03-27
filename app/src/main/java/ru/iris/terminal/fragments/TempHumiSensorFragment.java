package ru.iris.terminal.fragments;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.concurrent.ExecutionException;

import ru.iris.models.bus.Event;
import ru.iris.models.bus.devices.DeviceChangeEvent;
import ru.iris.models.database.Device;
import ru.iris.models.database.DeviceValue;
import ru.iris.models.protocol.data.DataLevel;
import ru.iris.models.protocol.enums.StandartDeviceValueLabel;
import ru.iris.terminal.R;
import ru.iris.terminal.httpapi.DeviceService;
import ru.iris.terminal.tasks.GetDeviceOperation;

import static ru.iris.terminal.activity.MainActivity.TAG;

public class TempHumiSensorFragment implements FragmentHandler {
    private DeviceService.DeviceIdent deviceIdent;
    private TextView nameLabel;
    private TextView tempLabel;
    private TextView humiLabel;

    @Override
    public void stop() {
        EventBus.getDefault().unregister(this);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View handle(LayoutInflater inflater, ViewGroup container, DeviceService.DeviceIdent deviceIdent) {
        View view = inflater.inflate(R.layout.temphumisensor, container, false);
        tempLabel = view.findViewById(R.id.temp);
        humiLabel = view.findViewById(R.id.humi);
        nameLabel = view.findViewById(R.id.name);

        EventBus.getDefault().register(this);

        updateDevice(deviceIdent);

        return view;
    }

    @Override
    public void updateData(DeviceService.DeviceIdent deviceIdent) {
        updateDevice(deviceIdent);
    }

    @SuppressLint("SetTextI18n")
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

            DeviceValue temp = device.getValues().get("temperature");
            DeviceValue humi = device.getValues().get("humidity");

            if(temp != null && temp.getCurrentValue() != null) {
                tempLabel.setText(temp.getCurrentValue() + "C");
            }

            if(humi != null && humi.getCurrentValue() != null) {
                humiLabel.setText(humi.getCurrentValue() + "%");
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @Subscribe
    public void onMessageEvent(DeviceChangeEvent e) {
        if(e.getProtocol().equals(deviceIdent.getSource()) && e.getChannel().equals(deviceIdent.getChannel())) {
            Log.d(TAG, "Event come to temphumi sensor fragment");
            DataLevel dataLevel = (DataLevel) e.getData();
            if(e.getEventLabel().equals(StandartDeviceValueLabel.TEMPERATURE.getName())) {
                tempLabel.setText(dataLevel.getTo() + "C");
            } else {
                humiLabel.setText(dataLevel.getTo() + "%");
            }
        }
    }
}
