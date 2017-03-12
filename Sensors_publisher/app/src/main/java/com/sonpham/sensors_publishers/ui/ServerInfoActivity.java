package com.sonpham.sensors_publishers.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.sonpham.sensors_publishers.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Activity demonstrates some GUI functionalities from the Android support library.
 * <p>
 * Created by Andreas Schrade on 14.12.2015.
 */
public class ServerInfoActivity extends BaseActivity {

    EditText et_host;
    EditText et_port;
    EditText et_pname;
    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        ButterKnife.bind(this);
        setupToolbar();

        et_host = (EditText) findViewById(R.id.server_text_host);
        et_port = (EditText) findViewById(R.id.server_text_port);
        et_pname = (EditText) findViewById(R.id.publisher_name);

        sharedpreferences = getSharedPreferences("server_info", this.MODE_PRIVATE);
        et_host.setText(sharedpreferences.getString("host_info", ListActivity.DEFAULT_HOST));
        et_port.setText("" + sharedpreferences.getInt("port_info", ListActivity.DEFAULT_PORT));
        et_pname.setText(sharedpreferences.getString("pname", ListActivity.DEFAULT_PUBLISHER_NAME));
    }

    @OnClick(R.id.fab)
    public void onFabClicked(View view) {
        Snackbar.make(view, "Server Info Updated", Snackbar.LENGTH_LONG).setAction("Action", null).show();

        SharedPreferences.Editor editor = sharedpreferences.edit();

        editor.putString("host_info", et_host.getText().toString());
        editor.putInt("port_info", Integer.parseInt(et_port.getText().toString()));
        editor.putString("pname", et_pname.getText().toString());
        editor.commit();
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

    @Override
    public boolean providesActivityToolbar() {
        return true;
    }

    @Override
    public void finish() {
        Intent data = new Intent();
        data.putExtra("host", et_host.getText().toString());
        data.putExtra("port", et_port.getText());
        setResult(RESULT_OK, data);
        super.finish();
    }

    /**
     * Handles the navigation item click and starts the corresponding activity.
     *
     * @param item the selected navigation item
     */
    @Override
    public void goToNavDrawerItem(int item) {
        Intent intent;
        switch (item) {
            case R.id.nav_main:
                finish();
                break;
            case R.id.nav_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
        }
    }
}
