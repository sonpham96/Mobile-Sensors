package com.sonpham.amqp_subscriber;

import android.app.LauncherActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.rabbitmq.client.ConnectionFactory;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    String mPublisherName;
    String mSensorType;
    GraphView mGraphView;
    private LineGraphSeries<DataPoint> mSeries;
    private double lastXvalue = 0d;

    ListView listView;
    ArrayAdapter<String> adapter;

    Subscriber subscribeThread;
    ConnectionFactory factory = new ConnectionFactory();
    Handler incomingMessageHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent i = getIntent();
        mPublisherName = i.getStringExtra(LaunchActivity.EXTRA_PUBLISHER_NAME);
        mSensorType = i.getStringExtra(LaunchActivity.EXTRA_SENSOR_TYPE);

        addControls();
        setupConnectionFactory();

        incomingMessageHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String message = msg.getData().getString("msg");
                final double d = Double.parseDouble(message);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSeries.appendData(new DataPoint(lastXvalue++,d),true, 100);
                    }
                });

                Date now = new Date();
                SimpleDateFormat ft = new SimpleDateFormat("hh:mm:ss");
                adapter.add(ft.format(now) + ' '+ mSensorType + ' ' + message + '\n');
            }
        };
        subscribeThread = new Subscriber(incomingMessageHandler, factory, mPublisherName, mSensorType);
        subscribeThread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        subscribeThread.interrupt();
    }

    private void addControls() {
        mGraphView = (GraphView) findViewById(R.id.graph);
        mSeries = new LineGraphSeries<DataPoint>();
        mGraphView.addSeries(mSeries);
        mGraphView.setTitle(mPublisherName +"\'s "+ mSensorType +" data");

        Viewport v = mGraphView.getViewport();
       // v.setBackgroundColor(R.color.backgroundDark);
        v.setBackgroundColor(Color.rgb(214, 221, 232));
        v.setScrollable(true);
        v.setXAxisBoundsManual(true);
        v.setMinX(0);
        v.setMaxX(100);

        GridLabelRenderer grid = mGraphView.getGridLabelRenderer();
        grid.setGridColor(android.R.color.white);



        listView = (ListView) findViewById(R.id.listView);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        listView.setAdapter(adapter);

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
