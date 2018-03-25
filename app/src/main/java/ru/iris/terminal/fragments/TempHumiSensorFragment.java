package ru.iris.terminal.fragments;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.iris.models.database.Device;
import ru.iris.models.database.DeviceValue;
import ru.iris.terminal.R;

public class TempHumiSensorFragment implements FragmentHandler {
    @SuppressLint("SetTextI18n")
    @Override
    public View handle(LayoutInflater inflater, ViewGroup container, Device device) {
        View view = inflater.inflate(R.layout.temphumisensor, container, false);
        TextView tempLabel = view.findViewById(R.id.temp);
        TextView humiLabel = view.findViewById(R.id.humi);
        TextView nameLabel = view.findViewById(R.id.name);

        DeviceValue temp = device.getValues().get("temperature");
        DeviceValue humi = device.getValues().get("humidity");

        nameLabel.setText(device.getHumanReadable());

        if(temp != null && temp.getCurrentValue() != null) {
            tempLabel.setText(temp.getCurrentValue() + "C");
        }

        if(humi != null && humi.getCurrentValue() != null) {
            humiLabel.setText(humi.getCurrentValue() + "%");
        }

        return view;
    }
}
