package ru.iris.terminal.httpapi.interfaces;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import ru.iris.models.database.Device;
import ru.iris.models.status.BackendAnswer;
import ru.iris.models.web.DeviceInfoRequest;
import ru.iris.models.web.DeviceSetLevelRequest;

public interface IDeviceService {
    @POST("device/get")
    Call<List<Device>> getDevices(@Body DeviceInfoRequest request);

    @POST("device/set")
    Call<BackendAnswer> setDeviceLevel(@Body DeviceSetLevelRequest request);
}
