package com.sonpham.sensors_publishers;

import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.rabbitmq.client.ConnectionFactory;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;


public class ListActivity extends BaseActivity implements SensorEventListener {
    private static final String TAG = ListActivity.class.getSimpleName().toUpperCase();

    // sensors
    private SensorManager mSensorManager;
    private Sensor mPressure;
    private Sensor mTemperature;
    private Sensor mLight;
    private int SENSOR_DELAY = SensorManager.SENSOR_DELAY_FASTEST; // in miliseconds, SENSOR_DELAY_GAME = 200,000 ms

    // listview
    private ListView list;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> stringList;

    // amqp broker
    private BlockingDeque<Message> queue = new LinkedBlockingDeque<>();
    ConnectionFactory factory = new ConnectionFactory();
    Publisher publishThread;
    String publisher_name;

    Handler handler;
    int interval = 250; // ms
    boolean[] flag = {false, false, false};
    private final Runnable processSensors = new Runnable() {
        @Override
        public void run() {
            flag[0] = sw_pressure;
            flag[1] = sw_light;
            flag[2] = sw_temperature;
            // The Runnable is posted to run again here
            handler.postDelayed(this, interval);
        }
    };

    // switches
    private boolean sw_pressure;
    private boolean sw_light;
    private boolean sw_temperature;

    // Socket
    Client client;
    String host;
    int port;

    SharedPreferences sharedpreferences;
    SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        addControls();
        setupToolbar();
        setupConnectionFactory();
        importSharedPreferences();

        (publishThread = new Publisher(queue, factory, publisher_name)).start();
    }

    private void addControls() {
        mSensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);
        addSensors();

        handler = new Handler();

        // set up listview
        list = (ListView) findViewById(R.id.list);
        stringList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, stringList);
        list.setAdapter(adapter);

        // shared preferences
        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    private void importSharedPreferences() {
        sw_pressure = PreferenceUtility.getSwitchPressure(sharedpreferences);
        sw_light = PreferenceUtility.getSwitchLight(sharedpreferences);
        sw_temperature = PreferenceUtility.getSwitchTemperature(sharedpreferences);
        publisher_name = PreferenceUtility.getPublisherName(sharedpreferences);
        host = PreferenceUtility.getHost(sharedpreferences);
        port = PreferenceUtility.getPort(sharedpreferences);
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

        if (flag[0] && eventSensor.getType() == Sensor.TYPE_PRESSURE) {
            message = "" + event.values[0];
            sendMessage("pressure", message);
            flag[0] = false;
        }
        if (flag[1] && eventSensor.getType() == Sensor.TYPE_LIGHT) {
            message = "" + event.values[0];
            sendMessage("light", message);
            flag[1] = false;
        }
        if (flag[2] && eventSensor.getType() == Sensor.TYPE_TEMPERATURE) {
            message = "" + event.values[0];
            sendMessage("temperature", message);
            flag[2] = false;
        }
    }

    private void sendMessage(String sensorType, String message) {
        Log.d(sensorType.toUpperCase() + "_SENSOR", message);
        adapter.add(dateFormat.format(new Date()) + " | " + sensorType + ": " + message);

        if (!client.isConnected()) {
            publishMessage(new Message(sensorType, message));
        } else {
            client.sendMessage(sensorType + '.' + message);
        }
    }

    private void publishMessage(Message msg) {
        //Adds a value to internal blocking queue
        try {
            Log.d(TAG, "[q] " + msg);
            queue.putLast(msg);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
            case R.id.action_clear:
                stringList.clear();
                adapter.notifyDataSetChanged();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_main;
    }

    @Override
    protected void onStart() {
        super.onStart();
        ((NavigationView) findViewById(R.id.nav_view)).setCheckedItem(getSelfNavDrawerItem());
        importSharedPreferences();
    }

    @Override
    protected void onResume() {
        // Register a listener for the sensor.
        super.onResume();
        Toast.makeText(this, host + ":" + port + "\n" + publisher_name, Toast.LENGTH_SHORT).show();

        client = new Client(host, port);
        if (publishThread == null) (publishThread = new Publisher(queue, factory, publisher_name)).start();

        mSensorManager.registerListener(this, mPressure, SENSOR_DELAY);
        mSensorManager.registerListener(this, mLight, SENSOR_DELAY);
        mSensorManager.registerListener(this, mTemperature, SENSOR_DELAY);

        handler.post(processSensors);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Be sure to unregister the sensor when the activity pauses.
        mSensorManager.unregisterListener(this);
        handler.removeCallbacks(processSensors);
    }

    @Override
    protected void onStop() {
        super.onStop();
        publishThread.interrupt();
        publishThread = null;
        client.interruptConnection();
        client = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceUtility.setSwitchPressure(sharedpreferences, false);
        PreferenceUtility.setSwitchLight(sharedpreferences, false);
        PreferenceUtility.setSwitchTemperature(sharedpreferences, false);
    }
}
