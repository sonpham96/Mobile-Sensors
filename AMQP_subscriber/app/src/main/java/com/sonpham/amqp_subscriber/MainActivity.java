package com.sonpham.amqp_subscriber;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.rabbitmq.client.ConnectionFactory;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    EditText et_publisherName;
    EditText et_sensorType;
    Button button;
    ListView listView;
    ArrayAdapter<String> adapter;

    Subscriber subscribeThread;
    ConnectionFactory factory = new ConnectionFactory();
    Handler incomingMessageHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addControls();
        setupConnectionFactory();

        incomingMessageHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String message = msg.getData().getString("msg");
                Date now = new Date();
                SimpleDateFormat ft = new SimpleDateFormat("hh:mm:ss");
                adapter.add(ft.format(now) + ' ' + message + '\n');
            }
        };
        subscribeThread = new Subscriber(incomingMessageHandler, factory);
        subscribeThread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        subscribeThread.interrupt();
    }

    private void addControls() {
        et_publisherName = (EditText) findViewById(R.id.et_publisherName);
        et_sensorType = (EditText) findViewById(R.id.et_sensorType);
        button = (Button) findViewById(R.id.button);

        listView = (ListView) findViewById(R.id.listView);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        listView.setAdapter(adapter);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subscribeThread.interrupt();
                subscribeThread = new Subscriber(incomingMessageHandler, factory);
                subscribeThread.publisherName = et_publisherName.getText().toString();
                subscribeThread.sensorType = et_sensorType.getText().toString();
                subscribeThread.start();
            }
        });
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
