package com.sonpham.sensors_publisher;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

    private SensorManager mSensorManager;
    private Sensor mPressure;
    private Sensor mTemperature;
    private Sensor mLight;
    private int SENSOR_DELAY = SensorManager.SENSOR_DELAY_NORMAL; // in miliseconds, SENSOR_DELAY_GAME = 200,000 ms

    private ListView list;
    private List<String> stringList;
    private ArrayAdapter<String> stringAdapter;

    private Switch sw_pressure;
    private Switch sw_light;
    private Switch sw_temperature;

    private BlockingDeque<String> queue = new LinkedBlockingDeque<String>();
    ConnectionFactory factory = new ConnectionFactory();
    Thread publishThread;

    boolean flag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        addControls();
        setupConnectionFactory();

        publishThread = new Publisher(queue, factory);
        publishThread.start();
    }

    private void addControls() {
        mSensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);
        addSensors();

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
        Sensor eventSensor = event.sensor;

        if (sw_pressure.isChecked() && eventSensor.getType() == Sensor.TYPE_PRESSURE) {
            float millibars_of_pressure = event.values[0];
            Log.d("PRESSURE_SENSOR:", "millibars_of_pressure: " + millibars_of_pressure);
            stringAdapter.add("PRESSURE: " + millibars_of_pressure);
            publishMessage("PRESSURE: " + millibars_of_pressure);
        }
        if (sw_light.isChecked() && eventSensor.getType() == Sensor.TYPE_LIGHT) {
            float lux = event.values[0];
            Log.d("LIGHT_SENSOR", "lux: " + lux);
            stringAdapter.add("LIGHT: " + lux);
            publishMessage("LIGHT: " + lux);
        }
        if (sw_temperature.isChecked() && eventSensor.getType() == Sensor.TYPE_TEMPERATURE) {
            float degree_of_Celsius = event.values[0];
            Log.d("TEMPERATURE_SENSOR", "degree_of_Celcius" + degree_of_Celsius);
            stringAdapter.add("TEMPERATURE: " + degree_of_Celsius);
            publishMessage("TEMPERATURE: " + degree_of_Celsius);
        }
    }

    @Override
    protected void onResume() {
        // Register a listener for the sensor.
        super.onResume();
        mSensorManager.registerListener(this, mPressure, SENSOR_DELAY);
        mSensorManager.registerListener(this, mLight, SENSOR_DELAY);
        mSensorManager.registerListener(this, mTemperature, SENSOR_DELAY);
    }

    @Override
    protected void onPause() {
        // Be sure to unregister the sensor when the activity pauses.
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        publishThread.interrupt();
    }

    void publishMessage(String message) {
        //Adds a message to internal blocking queue
        try {
            Log.d("","[q] " + message);
            queue.putLast(message);
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
}
