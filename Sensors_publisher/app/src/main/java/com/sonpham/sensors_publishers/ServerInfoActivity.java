package com.sonpham.sensors_publishers;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class ServerInfoActivity extends BaseActivity {
    EditText et_host;
    EditText et_port;
    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        ButterKnife.bind(this);
        setupToolbar();

        et_host = (EditText) findViewById(R.id.server_text_host);
        et_port = (EditText) findViewById(R.id.server_text_port);

        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        et_host.setText(PreferenceUtility.getHost(sharedpreferences));
        et_port.setText("" + PreferenceUtility.getPort(sharedpreferences));
    }

    @Override
    protected void onStart() {
        super.onStart();
        ((NavigationView) findViewById(R.id.nav_view)).setCheckedItem(getSelfNavDrawerItem());
    }

    @OnClick(R.id.fab)
    public void onFabClicked(View view) {
        Snackbar.make(view, "Server Info Updated", Snackbar.LENGTH_LONG).setAction("Action", null).show();

        PreferenceUtility.setHost(sharedpreferences, et_host.getText().toString());
        PreferenceUtility.setPort(sharedpreferences, Integer.parseInt(et_port.getText().toString()));
    }

    private void setupToolbar() {
        final ActionBar ab = getActionBarToolbar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu_24dp);
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
        return R.id.nav_server;
    }
}
