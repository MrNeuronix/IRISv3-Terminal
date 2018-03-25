package ru.iris.terminal.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.iris.models.database.Device;

public interface FragmentHandler {
    View handle(LayoutInflater inflater, ViewGroup container, Device device);
}
