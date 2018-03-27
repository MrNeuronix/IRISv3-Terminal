package ru.iris.terminal.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.iris.models.database.Device;
import ru.iris.terminal.httpapi.DeviceService;

public interface FragmentHandler {
    void updateData(DeviceService.DeviceIdent device);
    View handle(LayoutInflater inflater, ViewGroup container, DeviceService.DeviceIdent device);
    void stop();
}
