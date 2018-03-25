package ru.iris.terminal.activity;

import java.io.IOException;
import java.util.Random;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonProcessingException;

import ru.iris.models.database.Device;
import ru.iris.terminal.R;
import ru.iris.terminal.fragments.BinarySwitchFragment;
import ru.iris.terminal.fragments.BinaryTwoKeySwitchFragment;
import ru.iris.terminal.fragments.ControllerFragment;
import ru.iris.terminal.fragments.FragmentHandler;
import ru.iris.terminal.fragments.MainScreenFragment;
import ru.iris.terminal.fragments.TempHumiSensorFragment;
import ru.iris.terminal.fragments.TempSensorFragment;

public class PageFragment extends Fragment {

    private static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    private Device device;

    static PageFragment newInstance(int page, Device device) throws JsonProcessingException {
        PageFragment pageFragment = new PageFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARGUMENT_PAGE_NUMBER, page);

        if(device != null) {
            arguments.putString("device", MainActivity.objectMapper.writeValueAsString(device));
        }

        pageFragment.setArguments(arguments);
        return pageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int pageNumber = getArguments().getInt(ARGUMENT_PAGE_NUMBER);
        if(pageNumber != 0) {
            try {
                device = MainActivity.objectMapper.readValue(getArguments().getString("device"), Device.class);
            } catch (IOException e) {
                Log.d(MainActivity.TAG, "SERIALIZATION ERROR: " + e.getMessage());
            }
        }
    }

    @Override
    @SuppressLint({"InflateParams", "SetTextI18n"})
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
            return selectViewForDevice(inflater, container, device);
    }

    @SuppressLint("SetTextI18n")
    private View selectViewForDevice(LayoutInflater inflater, ViewGroup container, Device device) {
        FragmentHandler handler;

        if(device == null) {
            handler = new MainScreenFragment();
        } else {
            switch (device.getType()) {
                case CONTROLLER:
                    handler = new ControllerFragment();
                    break;
                case BINARY_SWITCH:
                    if(device.getValues().get("level2") != null) {
                        handler = new BinaryTwoKeySwitchFragment();
                    } else {
                        handler = new BinarySwitchFragment();
                    }
                    break;
                case TEMP_HUMI_SENSOR:
                    handler = new TempHumiSensorFragment();
                    break;
                case TEMP_SENSOR:
                    handler = new TempSensorFragment();
                    break;
                default:
                    handler = new MainScreenFragment();
            }
        }

        return handler.handle(inflater, container, device);
    }

}