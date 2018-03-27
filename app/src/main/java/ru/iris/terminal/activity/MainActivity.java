package ru.iris.terminal.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.WebSocket;
import ru.iris.models.bus.Event;
import ru.iris.terminal.R;
import ru.iris.terminal.httpapi.DeviceService;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.client.StompClient;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "IRISTERMINAL";
    public static final ObjectMapper objectMapper = new ObjectMapper();
    private SharedPreferences preferences;

    private static int PAGE_COUNT = 1;
    private static List<DeviceService.DeviceIdent> devices = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Зона #1");
        setContentView(R.layout.main);

        preferences = SplashActivity.getPreferences();

        getDevices();
        connectStomp();

        PAGE_COUNT = devices.size()+1; // 1 - это будет главный экран с часами погодой и прочей херней

        Log.d(MainActivity.TAG, "This zone contained " + devices.size() + " devices");

        ViewPager pager = findViewById(R.id.pager);
        PagerAdapter pagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);

        pager.addOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "onPageSelected, position = " + position);
                pagerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void getDevices() {
        String devicesJson = getIntent().getStringExtra("devices");
        try {
            devices = objectMapper.readValue(devicesJson, new TypeReference<ArrayList<DeviceService.DeviceIdent>>(){});
        } catch (IOException e) {
            Log.d(MainActivity.TAG, "SERIALIZATION ERROR: " + e.getMessage());
        }
    }

    private void connectStomp() {
        String url = "ws://" + preferences.getString("server", "localhost") + "/stomp/websocket";
        Log.d(TAG, "Base STOMP URL is " + url);
        StompClient mStompClient = Stomp.over(WebSocket.class, url);

        mStompClient.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(lifecycleEvent -> {
                    switch (lifecycleEvent.getType()) {
                        case OPENED:
                            Log.e(TAG, "Stomp connection opened");
                            break;
                        case ERROR:
                            Log.e(TAG, "Stomp connection error", lifecycleEvent.getException());
                            break;
                        case CLOSED:
                            Log.e(TAG, "Stomp connection closed");
                    }
                });

        // Receive
        mStompClient.topic("/topic/event")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(topicMessage -> {
                    Log.d(TAG, "Received " + topicMessage.getPayload());
                    EventBus.getDefault().post(objectMapper.readValue(topicMessage.getPayload(), Event.class));
                });

        mStompClient.connect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menu.getItem(0).setIntent(new Intent(this, PreferencesActivity.class));
        return super.onCreateOptionsMenu(menu);
    }

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {
        MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            PageFragment f = (PageFragment ) object;
            f.update();
            return super.getItemPosition(object);
        }

        @Override
        public Fragment getItem(int position) {
            try {
                if(position != 0) {
                    return PageFragment.newInstance(position, devices.get(position - 1));
                } else {
                    return PageFragment.newInstance(0, null);
                }
            } catch (JsonProcessingException e) {
                Log.d(MainActivity.TAG, "SERIALIZATION ERROR: " + e.getMessage());
                return null;
            }
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }
    }
}