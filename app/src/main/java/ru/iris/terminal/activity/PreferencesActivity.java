package ru.iris.terminal.activity;



import android.os.Bundle;
import android.preference.PreferenceActivity;

import ru.iris.terminal.R;

public class PreferencesActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.PrefStyle);
        addPreferencesFromResource(R.xml.preferences);
    }
}