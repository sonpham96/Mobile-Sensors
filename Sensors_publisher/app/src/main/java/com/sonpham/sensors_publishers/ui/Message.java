package com.sonpham.sensors_publishers.ui;

/**
 * Created by sonpham on 2017/03/02.
 */

public class Message {
    public String sensorType;
    public String value;

    public Message(String sensorType, String value) {
        this.sensorType = sensorType;
        this.value = value;
    }
}
