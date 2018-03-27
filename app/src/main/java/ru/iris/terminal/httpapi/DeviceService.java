package ru.iris.terminal.httpapi;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.IOException;
import java.util.List;

import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import ru.iris.models.database.Device;
import ru.iris.models.protocol.enums.DeviceType;
import ru.iris.models.protocol.enums.SourceProtocol;
import ru.iris.models.status.BackendAnswer;
import ru.iris.models.web.DeviceInfoRequest;
import ru.iris.models.web.DeviceSetLevelRequest;
import ru.iris.terminal.activity.SplashActivity;
import ru.iris.terminal.httpapi.interfaces.IDeviceService;

import static ru.iris.terminal.activity.MainActivity.TAG;

public class DeviceService {
    private static IDeviceService service;
    private static DeviceService instance;

    public static class DeviceIdent {
        private SourceProtocol source;
        private String channel;
        private DeviceType type;

        public DeviceIdent() {
        }

        public DeviceIdent(SourceProtocol source, String channel, DeviceType type) {
            this.source = source;
            this.channel = channel;
            this.type = type;
        }

        public SourceProtocol getSource() {
            return source;
        }

        public String getChannel() {
            return channel;
        }

        public DeviceType getType() {
            return type;
        }
    }

    private DeviceService() {
        SharedPreferences preferences = SplashActivity.getPreferences();
        String url = "http://" + preferences.getString("server", "localhost") + "/api/";

        Log.d(TAG, "Base API URL is " + url);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        service = retrofit.create(IDeviceService.class);
        instance = this;
    }

    public static void destroy() {
        instance = null;
    }

    public static synchronized DeviceService getInstance() {
        if(instance != null) {
            return instance;
        } else {
            instance = new DeviceService();
            return instance;
        }
    }

    public List<Device> getDevices(SourceProtocol source, String channel) throws IOException {
        return service.getDevices(new DeviceInfoRequest(source, channel)).execute().body();
    }

    public BackendAnswer setDeviceLevel(SourceProtocol source, String channel, Integer subchannel, String value) throws IOException {
        return service.setDeviceLevel(new DeviceSetLevelRequest(source, channel, subchannel, value)).execute().body();
    }
}
