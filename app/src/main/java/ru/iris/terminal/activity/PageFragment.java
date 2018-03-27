package ru.iris.terminal.activity;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fasterxml.jackson.core.JsonProcessingException;

import ru.iris.terminal.fragments.BinarySwitchFragment;
import ru.iris.terminal.fragments.BinaryTwoKeySwitchFragment;
import ru.iris.terminal.fragments.ControllerFragment;
import ru.iris.terminal.fragments.DoorSensorFragment;
import ru.iris.terminal.fragments.FragmentHandler;
import ru.iris.terminal.fragments.MainScreenFragment;
import ru.iris.terminal.fragments.TempHumiSensorFragment;
import ru.iris.terminal.fragments.TempSensorFragment;
import ru.iris.terminal.fragments.Updatable;
import ru.iris.terminal.httpapi.DeviceService;

public class PageFragment extends Fragment implements Updatable {

    private static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    private DeviceService.DeviceIdent device;
    private FragmentHandler handler;

    static PageFragment newInstance(int page, DeviceService.DeviceIdent deviceIdent) throws JsonProcessingException {
        PageFragment pageFragment = new PageFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARGUMENT_PAGE_NUMBER, page);

        if(deviceIdent != null) {
            arguments.putString("device", MainActivity.objectMapper.writeValueAsString(deviceIdent));
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
                device = MainActivity.objectMapper.readValue(getArguments().getString("device"), DeviceService.DeviceIdent.class);
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
    private View selectViewForDevice(LayoutInflater inflater, ViewGroup container, DeviceService.DeviceIdent device) {

        if(device == null) {
            handler = new MainScreenFragment();
        } else {
            switch (device.getType()) {
                case CONTROLLER:
                    handler = new ControllerFragment();
                    break;
                case BINARY_SWITCH:
                    handler = new BinarySwitchFragment();
                    break;
                case BINARY_SWITCH_TWO_BUTTONS:
                    handler = new BinaryTwoKeySwitchFragment();
                    break;
                case TEMP_HUMI_SENSOR:
                    handler = new TempHumiSensorFragment();
                    break;
                case TEMP_SENSOR:
                    handler = new TempSensorFragment();
                    break;
                case DOOR_SENSOR:
                    handler = new DoorSensorFragment();
                    break;
                default:
                    handler = new MainScreenFragment();
            }
        }

        return handler.handle(inflater, container, device);
    }

    @Override
    public void update() {
        handler.updateData(device);
    }

    @Override
    public void onStop() {
        super.onStop();
        handler.stop();
    }
}