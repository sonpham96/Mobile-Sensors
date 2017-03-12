package com.sonpham.sensors_publishers.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.rabbitmq.client.ConnectionFactory;
import com.sonpham.sensors_publishers.R;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;


public class ListActivity extends BaseActivity implements SensorEventListener {
    // sensors
    private SensorManager mSensorManager;
    private Sensor mPressure;
    private Sensor mTemperature;
    private Sensor mLight;
    private int SENSOR_DELAY = SensorManager.SENSOR_DELAY_FASTEST; // in miliseconds, SENSOR_DELAY_GAME = 200,000 ms

    // listview
    private ListView list;
    private ArrayAdapter<String> adapter;

    // switches
    private boolean sw_pressure;
    private boolean sw_light;
    private boolean sw_temperature;

    // amqp broker
    private BlockingDeque<Message> queue = new LinkedBlockingDeque<Message>();
    ConnectionFactory factory = new ConnectionFactory();
    Publisher publishThread;
    Handler handler;
    int interval = 250; // ms
    boolean[] flag = {true, false, false};
    int turn = 0;

    // Socket
    Client client;
    String host = "192.168.100.7";
    int port = 2000;
    public static final String DEFAULT_HOST = "";
    public static final int DEFAULT_PORT = 2000;
    public static final String DEFAULT_PUBLISHER_NAME = "";
    String publisher_name;

    public static final int REQUEST_CODE_SETTINGS = 222;
    public static final int REQUEST_CODE_SERVER = 333;

    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        addControls();
        setupToolbar();
        setupConnectionFactory();

        sharedpreferences = getSharedPreferences("server_info", this.MODE_PRIVATE);
        host = sharedpreferences.getString("host_info", DEFAULT_HOST);
        port = sharedpreferences.getInt("port_info", DEFAULT_PORT);
        publisher_name = sharedpreferences.getString("pname", DEFAULT_PUBLISHER_NAME);
        Toast.makeText(this, host + ":" + port + "\n" + publisher_name, Toast.LENGTH_SHORT).show();

        client = new Client(host, port);
        publishThread = new Publisher(queue, factory, publisher_name);
        publishThread.start();
    }

    private void addControls() {
        mSensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);
        addSensors();

        handler = new Handler();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        list = (ListView) findViewById(R.id.list);
        list.setAdapter(adapter);
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
        return R.id.nav_main;
    }

    @Override
    public boolean providesActivityToolbar() {
        return false;
    }

    /**
     * Handles the navigation item click and starts the corresponding activity.
     *
     * @param item the selected navigation item
     */
    @Override
    public void goToNavDrawerItem(int item) {
        switch (item) {
            case R.id.nav_server:
                startActivityForResult(new Intent(this, ServerInfoActivity.class),
                        REQUEST_CODE_SERVER);

                break;
            case R.id.nav_settings:
                startActivityForResult(new Intent(this, SettingsActivity.class),
                        REQUEST_CODE_SETTINGS);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_SERVER:
                    if (data.hasExtra("host") && data.hasExtra("port")) {
//                        client.thread.interrupt();
                        host = sharedpreferences.getString("host_info", DEFAULT_HOST);
                        port = sharedpreferences.getInt("port_info", DEFAULT_PORT);
                        client = new Client(host, port);
                        publisher_name = sharedpreferences.getString("pname", DEFAULT_PUBLISHER_NAME);
                        publishThread.publisherName = publisher_name;
                    }
                    break;
                case REQUEST_CODE_SETTINGS:
                    if (data.hasExtra("pressure") && data.hasExtra("light") && data.hasExtra("temp")) {
                        sw_pressure = data.getExtras().getBoolean("pressure");
                        sw_light = data.getExtras().getBoolean("light");
                        sw_temperature = data.getExtras().getBoolean("temp");
                    }
            }
        }
    }

    private void setupConnectionFactory() {
        String uri = "amqp://cgpwrecl:HahPfY4iosAO946_prpD0SxJ4ao4fe7O@white-mynah-bird.rmq.cloudamqp.com/cgpwrecl";
        try {
            factory.setAutomaticRecoveryEnabled(false);
            factory.setUri(uri);
        } catch (KeyManagementException | NoSuchAlgorithmException | URISyntaxException e1) {
            e1.printStackTrace();
        }
    }

    private void addSensors() {
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) != null)
            mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null)
            mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE) != null)
            mTemperature = mSensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE);
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        Sensor eventSensor = event.sensor;
        String message;

        if (sw_pressure && eventSensor.getType() == Sensor.TYPE_PRESSURE && flag[0] && turn == 0) {
            float millibars_of_pressure = event.values[0];
            message = "" + millibars_of_pressure;
            Log.d("PRESSURE_SENSOR:", "millibars_of_pressure" + message);
            adapter.add(message);
            sendMessage("pressure", message);
            flag[0] = false;
        }
        if (sw_light && eventSensor.getType() == Sensor.TYPE_LIGHT && flag[1] && turn == 1) {
            float lux = event.values[0];
            message = "" + lux;
            Log.d("LIGHT_SENSOR", "lux: " + message);
            adapter.add(message);
            sendMessage("light", message);
            flag[1] = false;
        }
        if (sw_temperature && eventSensor.getType() == Sensor.TYPE_TEMPERATURE && flag[2] && turn == 2) {
            float degree_of_Celsius = event.values[0];
            message = "" + degree_of_Celsius;
            Log.d("TEMPERATURE_SENSOR", "degree_of_celsius: " + message);
            adapter.add(message);
            sendMessage("temperature", message);
            flag[2] = false;
        }
    }

    private void sendMessage(String sensorType, String message) {
        if (!client.isConnected()) {
            publishMessage(new Message(sensorType, message));
        } else {
            client.sendMessage(sensorType + '.'  + message);
        }
    }

    void publishMessage(Message msg) {
        //Adds a value to internal blocking queue
        try {
            Log.d("", "[q] " + msg);
            queue.putLast(msg);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        // Register a listener for the sensor.
        super.onResume();
        mSensorManager.registerListener(this, mPressure, SENSOR_DELAY);
        mSensorManager.registerListener(this, mLight, SENSOR_DELAY);
        mSensorManager.registerListener(this, mTemperature, SENSOR_DELAY);
//        client = new Client(host, port);

        handler.post(processSensors);
    }

    @Override
    protected void onPause() {
        // Be sure to unregister the sensor when the activity pauses.
        super.onPause();
        mSensorManager.unregisterListener(this);
        handler.removeCallbacks(processSensors);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        publishThread.interrupt();
    }

    private final Runnable processSensors = new Runnable() {
        @Override
        public void run() {
            flag[turn] = true;
            turn = (turn + 1) % 3;
            // The Runnable is posted to run again here
            handler.postDelayed(this, interval);
        }
    };
}
