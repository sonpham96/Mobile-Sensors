package com.sonpham.amqp_subscriber;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

public class LaunchActivity extends AppCompatActivity {
    public static final String EXTRA_PUBLISHER_NAME = "com.example.anh.livegraph.publisher_name";
    public static final String EXTRA_SENSOR_TYPE = "com.example.anh.livegraph.sensor_type";
    EditText et_publisherName;
    FloatingActionButton light_button;
    FloatingActionButton temp_button;
    FloatingActionButton pressure_button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        et_publisherName = (EditText) findViewById(R.id.et_publisherName);
        light_button = (FloatingActionButton) findViewById(R.id.light_button);
        temp_button = (FloatingActionButton) findViewById(R.id.temp_button);
        pressure_button = (FloatingActionButton) findViewById(R.id.pressure_button);

        light_button.setOnClickListener(generateListner("light"));
        temp_button.setOnClickListener(generateListner("temperature"));
        pressure_button.setOnClickListener(generateListner("pressure"));
    }

    public View.OnClickListener generateListner(String sensorType) {
        final String type = sensorType;

        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LaunchActivity.this, MainActivity.class);
                i.putExtra(EXTRA_PUBLISHER_NAME, et_publisherName.getText().toString());
                i.putExtra(EXTRA_SENSOR_TYPE, type);

                startActivity(i);
            }
        };
    }
}
