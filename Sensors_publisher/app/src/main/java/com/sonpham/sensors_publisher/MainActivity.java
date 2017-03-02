package com.sonpham.sensors_publisher;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import com.rabbitmq.client.ConnectionFactory;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // sensors
    private SensorManager mSensorManager;
    private Sensor mPressure;
    private Sensor mTemperature;
    private Sensor mLight;
    private int SENSOR_DELAY = SensorManager.SENSOR_DELAY_NORMAL; // in miliseconds, SENSOR_DELAY_GAME = 200,000 ms

    // listview
    private ListView list;
    private List<Float> stringList;
    private ArrayAdapter<Float> stringAdapter;

    // switches
    private Switch sw_pressure;
    private Switch sw_light;
    private Switch sw_temperature;

    // amqp broker
    private BlockingDeque<Message> queue = new LinkedBlockingDeque<Message>();
    ConnectionFactory factory = new ConnectionFactory();
    Thread publishThread;
    Handler handler;
    int interval = 500; // ms
    boolean flag = true;

    // Socket
    Client client;
    String host = "192.168.100.7";
    int port = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addControls();
        setupConnectionFactory();
        client = new Client(host, port);

        publishThread = new Publisher(queue, factory);
        publishThread.start();
    }

    private void addControls() {
        mSensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);
        addSensors();

        handler = new Handler();

        sw_pressure = (Switch) findViewById(R.id.sw_pressure);
        sw_light = (Switch) findViewById(R.id.sw_light);
        sw_temperature = (Switch) findViewById(R.id.sw_temperature);

        stringList = new ArrayList<>();
        stringAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, stringList);

        list = (ListView) findViewById(R.id.list);
        list.setAdapter(stringAdapter);
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
        if (flag) {
            Sensor eventSensor = event.sensor;
            String message;

            if (sw_pressure.isChecked() && eventSensor.getType() == Sensor.TYPE_PRESSURE) {
                float millibars_of_pressure = event.values[0];
                message = "" + millibars_of_pressure;
                Log.d("PRESSURE_SENSOR:", "millibars_of_pressure" + message);
                stringAdapter.add(Float.valueOf(stringToFloat(message)));
                sendMessage("pressure", message);
            }
            if (sw_light.isChecked() && eventSensor.getType() == Sensor.TYPE_LIGHT) {
                float lux = event.values[0];
                message = "" + lux;
                Log.d("LIGHT_SENSOR", "lux: " + message);
                stringAdapter.add(Float.valueOf(stringToFloat(message)));
                sendMessage("light", message);
            }
            if (sw_temperature.isChecked() && eventSensor.getType() == Sensor.TYPE_TEMPERATURE) {
                float degree_of_Celsius = event.values[0];
                message = "" + degree_of_Celsius;
                Log.d("TEMPERATURE_SENSOR", "degree_of_celsius: " + message);
                stringAdapter.add(Float.valueOf(stringToFloat(message)));
                sendMessage("temperature", message);
            }
            flag = false;
        }
    }

    private void sendMessage(String sensorType, String message) {
        if (!client.isConnected()) {
            publishMessage(new Message(sensorType, message));
        } else {
            client.sendMessage(message);
        }
    }

    private float stringToFloat(String s) {
//        char delim = ' ';
//        int delimIdx = s.indexOf(delim);
//        String newString = s.substring(delimIdx + 1, s.length());
        return Float.parseFloat(s);
    }

    @Override
    protected void onResume() {
        // Register a listener for the sensor.
        super.onResume();
        mSensorManager.registerListener(this, mPressure, SENSOR_DELAY);
        mSensorManager.registerListener(this, mLight, SENSOR_DELAY);
        mSensorManager.registerListener(this, mTemperature, SENSOR_DELAY);

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

    void publishMessage(Message msg) {
        //Adds a value to internal blocking queue
        try {
            Log.d("", "[q] " + msg);
            queue.putLast(msg);
        } catch (InterruptedException e) {
            e.printStackTrace();
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

    private final Runnable processSensors = new Runnable() {
        @Override
        public void run() {
            flag = true;
            // The Runnable is posted to run again here
            handler.postDelayed(this, interval);
        }
    };
}
