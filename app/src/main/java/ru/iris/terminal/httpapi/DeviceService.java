package ru.iris.terminal.httpapi;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;
import java.util.List;

import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import ru.iris.models.database.Device;
import ru.iris.models.protocol.enums.SourceProtocol;
import ru.iris.models.status.BackendAnswer;
import ru.iris.models.web.DeviceInfoRequest;
import ru.iris.models.web.DeviceSetLevelRequest;
import ru.iris.terminal.httpapi.interfaces.IDeviceService;

public class DeviceService {
    private static IDeviceService service;
    private static DeviceService instance;

    public DeviceService(Activity activity) {
        SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(preferences.getString("server", "http://192.168.10.58:9000/api/"))
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        service = retrofit.create(IDeviceService.class);
        instance = this;
    }

    public static DeviceService getInstance() {
        return instance;
    }

    public List<Device> getDevices(SourceProtocol source, String channel) throws IOException {
        return service.getDevices(new DeviceInfoRequest(source, channel)).execute().body();
    }

    public BackendAnswer setDeviceLevel(SourceProtocol source, String channel, Integer subchannel, String value) throws IOException {
        return service.setDeviceLevel(new DeviceSetLevelRequest(source, channel, subchannel, value)).execute().body();
    }
}
