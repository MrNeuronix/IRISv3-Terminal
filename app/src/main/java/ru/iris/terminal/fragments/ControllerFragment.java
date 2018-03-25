package ru.iris.terminal.fragments;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.iris.models.database.Device;
import ru.iris.terminal.R;

public class ControllerFragment implements FragmentHandler {
    @Override
    public View handle(LayoutInflater inflater, ViewGroup container, Device device) {
        View view = inflater.inflate(R.layout.controller, container, false);
        TextView tvPage = view.findViewById(R.id.tvPage);
        tvPage.setBackgroundColor(Color.argb(255, 255, 0, 0));
        tvPage.setText("CONTROLLER: " + device.getSource() + "/" + device.getChannel());
        return view;
    }
}
