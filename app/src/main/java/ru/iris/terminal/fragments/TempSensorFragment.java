package ru.iris.terminal.fragments;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.iris.models.database.Device;
import ru.iris.models.database.DeviceValue;
import ru.iris.terminal.R;

public class TempSensorFragment implements FragmentHandler {
    @SuppressLint("SetTextI18n")
    @Override
    public View handle(LayoutInflater inflater, ViewGroup container, Device device) {
        View view = inflater.inflate(R.layout.tempsensor, container, false);
        TextView tempLabel = view.findViewById(R.id.temp);
        TextView nameLabel = view.findViewById(R.id.name);

        nameLabel.setText(device.getHumanReadable());

        DeviceValue temp = device.getValues().get("temperature");

        if(temp != null && temp.getCurrentValue() != null) {
            tempLabel.setText(temp.getCurrentValue() + "C");
        }
        return view;
    }
}
