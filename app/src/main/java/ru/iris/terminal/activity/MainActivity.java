package ru.iris.terminal.activity;

import android.os.Bundle;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.iris.models.database.Device;
import ru.iris.terminal.R;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "TERMINAL";
    public static final ObjectMapper objectMapper = new ObjectMapper();

    private static int PAGE_COUNT = 1;
    private static List<Device> devices = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Зона #1");
        setContentView(R.layout.main);

        getDevices();
        PAGE_COUNT = devices.size()+1; // 1 - это будет главный экран с часами погодой и прочей херней

        Log.d(MainActivity.TAG, "This zone contained " + devices.size() + " devices");

        ViewPager pager = findViewById(R.id.pager);
        PagerAdapter pagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);

        pager.addOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "onPageSelected, position = " + position);
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
            devices = objectMapper.readValue(devicesJson, new TypeReference<ArrayList<Device>>(){});
        } catch (IOException e) {
            Log.d(MainActivity.TAG, "SERIALIZATION ERROR: " + e.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {
        MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
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