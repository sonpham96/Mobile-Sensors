package com.sonpham.sensors_publishers.ui;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import com.sonpham.sensors_publishers.R;
import com.sonpham.sensors_publishers.ui.base.BaseActivity;
import com.sonpham.sensors_publishers.util.LogUtil;

/**
 * This Activity provides several settings. Activity contains {@link PreferenceFragment} as inner class.
 *
 * Created by Andreas Schrade on 14.12.2015.
 */
public class SettingsActivity extends BaseActivity {

    static PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setupToolbar();
    }

    private void setupToolbar() {
        final ActionBar ab = getActionBarToolbar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_action, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                openDrawer();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_settings;
    }

    @Override
    public boolean providesActivityToolbar() {
        return true;
    }

    public static class SettingsFragment extends PreferenceFragment {
        public SettingsFragment() {}

        @Override
        public void onCreate(Bundle savedInstanceState) {
            LogUtil.logD("tag", "Settings created");
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_prefs);
            preferenceManager = getPreferenceManager();
        }
    }

    @Override
    public void finish() {
        Intent data = new Intent();
        data.putExtra("pressure", preferenceManager.getSharedPreferences().getBoolean("pref_settings_1", false));
        data.putExtra("light", preferenceManager.getSharedPreferences().getBoolean("pref_settings_2", false));
        data.putExtra("temp", preferenceManager.getSharedPreferences().getBoolean("pref_settings_3", false));
        setResult(RESULT_OK, data);
        super.finish();
    }

    /**
     * Handles the navigation item click and starts the corresponding activity.
     * @param item the selected navigation item
     */
    @Override
    public void goToNavDrawerItem(int item) {
        Intent intent;
        switch (item) {
            case R.id.nav_main:
                finish();
                break;
            case R.id.nav_server:
                intent = new Intent(this, ServerInfoActivity.class);
                startActivity(intent);
                break;
        }
    }
}
