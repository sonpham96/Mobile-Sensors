# Sensors_publisher a.k.a sensorUp

## What is this?

This is a simple Android app that collects the device's sensors data and sends them to a server (TCP) or a broker (AMQP).


## Dependencies

* AppCompat Support Library
* Support Design Library
* Butterknife
* RabbitMQ Java Client Library
* SLF4J API
* SLF4J Simple

## Supported devices

The app supports every device with a SDK level of at least 14 (Android 4.0+).


## Brief Implementation

### Gradle
[/app/build.gradle](/Sensors_publisher/app/build.gradle)

```xml
    compile "com.android.support:appcompat-v7:${android_support_lib_version}"
    compile "com.android.support:design:${android_support_lib_version}"
    compile 'com.jakewharton:butterknife:7.0.1'
    compile 'com.rabbitmq:amqp-client:4.1.0'
    compile 'org.slf4j:slf4j-api:1.7.24'
    compile 'org.slf4j:slf4j-simple:1.7.24'
```

### Manifest
[/app/src/main/AndroidManifest.xml](/Sensors_publisher/app/src/main/AndroidManifest.xml)

```xml
> <uses-permission android:name="android.permission.INTERNET" />
```

To be able to setup TCP connection and AQMP connection, the app need to request for the permission to access the Internet.


### Source code
[/app/src/main/java/com/sonpham/sensors_publishers](/Sensors_publisher/app/src/main/java/com/sonpham/sensors_publishers)

#### [BaseActivity.java](/Sensors_publisher/app/src/main/java/com/sonpham/sensors_publishers/BaseActivity.java), the base class
*BaseActivity* is the parent class for every *Activity* inside this project. This class creates and provides the navigation drawer and toolbar.

#### [ListActivity.java](/Sensors_publisher/app/src/main/java/com/sonpham/sensors_publishers/ListActivity.java), the main activity

*ListActivity* can be considered as the main activity, the activity that you will mainly work on.

```java
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
```

Retrieve data from prefered sensors *(flag)* everytime there is a change in the value with the update interval of 250 ms *(interval)*.

```java
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

private void setupConnectionFactory() {
	String uri = "CLOUDAMQP_URL";
	try {
		factory.setAutomaticRecoveryEnabled(false);
		factory.setUri(uri);
	} catch (KeyManagementException | NoSuchAlgorithmException | URISyntaxException e1) {
		e1.printStackTrace();
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
```

Setup the connection factory *(setupConnectionFactory)*, start new publisher instance and establish the connection *(onCreate)*, put the message inside the queue queueing for its turn to be published *(publishMessage)*.

```java
private void sendMessage(String sensorType, String message) {
	Log.d(sensorType.toUpperCase() + "_SENSOR", message);
	adapter.add(dateFormat.format(new Date()) + " | " + sensorType + ": " + message);

	if (!client.isConnected()) {
		publishMessage(new Message(sensorType, message));
	} else {
		client.sendMessage(sensorType + '.' + message);
	}
}
```

Switch to sending data directly to AMQP server whenever the Gateway is not available.

#### [ServerInfoActivity.java](/Sensors_publisher/app/src/main/java/com/sonpham/sensors_publishers/ServerInfoActivity.java), the server info

*ServerInfoActivity* is where you configure the information of the host and the port of the Gateway the app will connect to. The system will attempt to reconnect to the server after every modification.

#### [SettingsActivity.java](/Sensors_publisher/app/src/main/java/com/sonpham/sensors_publishers/SettingsActivity.java), your preferences

*SettingsActivity* provides 3 switches to determine what type of sensor data should be collected as well as an input field to configure the publisher name, the tag of the data source. Note that you can collect data from many sensors simultaneously.

#### [Client.java](/Sensors_publisher/app/src/main/java/com/sonpham/sensors_publishers/Client.java), the TCP connection

*Client* provides the definition and the protocol between the device and the Gateway.

```java
public Client(String host, int port) {
	this.host = host;
	this.port = port;
	online = false;
	socket = new Socket();

	establishConnection();
}

private void establishConnection() {
	connection = new Thread(new Runnable() {
		@Override
		public void run() {
			while (!isConnected() && !Thread.currentThread().isInterrupted()) {
				try {
					Log.d(TAG, "Connecting to /" + host + ":" + port + "...");
					socket = new Socket();
					socket.connect(new InetSocketAddress(host, port), TIME_OUT);
					online = true;
					Log.d(TAG, "Connected to " + socket.getRemoteSocketAddress());
					out = new DataOutputStream(socket.getOutputStream());
				} catch (IOException e) {
					Log.d(TAG, "ERROR: CONNECTION FAILED");
					online = false;
				}
			}
		}
	});
	connection.start();
}

public void sendMessage(String msg) {
	Log.d(TAG, "Your message: " + msg);

	try {
		out.writeUTF(msg);
	} catch (IOException e) {
		if (online) {
			online = false;
			establishConnection();
		}
	}
}
```

Establish the connection when a *Client* instance is created (*Client constructor* and *establishConnection*), send messages and when the message cannot be sent try to reconnect to the server *(sendMessage)*.

#### [Publisher.java](/Sensors_publisher/app/src/main/java/com/sonpham/sensors_publishers/Publisher.java), the AMQP connection

*Publisher* has the definition and the protocol between the device and the AMQP server.

```java
public class Publisher extends Thread {
	private BlockingDeque<Message> queue;
    private ConnectionFactory factory;
    private static final String EXCHANGE_NAME = "amq.topic";
    public String publisherName;
	...
    
    @Override
    public void run() {
        while (true) {
            try {
                Connection connection = factory.newConnection();
                Channel ch = connection.createChannel();
                ch.confirmSelect();

                while (true) {
                    Message message = queue.takeFirst();
                    try {
                        ch.basicPublish(EXCHANGE_NAME, publisherName + '.' + message.sensorType, null, message.value.getBytes());
                        Log.d(TAG, "[s] " + message.value);
                        ch.waitForConfirmsOrDie();
                    } catch (Exception e) {
                        Log.d(TAG, "[f] " + message.value);
                        queue.putFirst(message);
                        throw e;
                    }
                }
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                Log.d(TAG, "Connection broken: " + e.getClass().getName());
                try {
                    Thread.sleep(5000); //sleep and then try again
                } catch (InterruptedException e1) {
                    break;
                }
            }
        }
    }
}
```

Whenever, *Publisher* instance is created and run *(Thread)*, the thread will get queue data in background and publish them to the AMQP server continuously. We use the *topic* exchange *(amq.topic)* to enable the binding of routing key, a message sent with a particular routing key will be delivered to all the queues that are bound with a matching binding key.
##### ![topic exchange](https://www.rabbitmq.com/img/tutorials/python-five.png)

#### [Message.java](/Sensors_publisher/app/src/main/java/com/sonpham/sensors_publishers/Message.java), the Message class

The definition of the type Message that will be transfered throughout the system. Includes: type of the sensor and the name of the publisher.

```java
public class Message {
    public String sensorType;
    public String value;

    public Message(String sensorType, String value) {
        this.sensorType = sensorType;
        this.value = value;
    }
}
```

#### [PreferenceUtility.java](/Sensors_publisher/app/src/main/java/com/sonpham/sensors_publishers/PreferenceUtility.java), a helper class

*PreferenceUtility* provides methods for getting and setting the saved preferences.


## Usage

In [ListActivity.java](/Sensors_publisher/app/src/main/java/com/sonpham/sensors_publishers/ListActivity.java), change the value of uri value to the URL can be found in the control panel for your instance.

```java
private void setupConnectionFactory() {
	String uri = "CLOUDAMQP_URL";
	try {
		factory.setAutomaticRecoveryEnabled(false);
		factory.setUri(uri);
	} catch (KeyManagementException | NoSuchAlgorithmException | URISyntaxException e1) {
		e1.printStackTrace();
	}
}
```