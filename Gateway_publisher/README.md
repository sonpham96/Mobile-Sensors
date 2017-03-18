# Gateway_publisher a.k.a the Gateway


## What is this?

This is a simple Java implementation that act as a server that gathers the sensors data from clients and sends them to an AMQP server.


## Dependencies

* RabbitMQ Java Client Library
* SLF4J API
* SLF4J Simple

## Supported devices

Every devices that has the Java Virtual Machine (JVM) installed and a decent Internet connection.

## Brief Implementation

### Maven
[/Gateway_publisher/pom.xml](/pom.xml)

```xml
<dependencies>
	<dependency>
		<groupId>com.rabbitmq</groupId>
		<artifactId>amqp-client</artifactId>
		<version>4.1.0</version>
	</dependency>

	<dependency>
		<groupId>org.slf4j</groupId>
		<artifactId>slf4j-api</artifactId>
		<version>1.7.24</version>
	</dependency>

	<dependency>
		<groupId>org.slf4j</groupId>
		<artifactId>slf4j-simple</artifactId>
		<version>1.7.24</version>
	</dependency>
</dependencies>
```

### Source code
[/Gateway_publisher/src/main/java](/src/main/java)

#### [Publisher.java](/Gateway_publisher/src/main/java/AMQP/Publisher.java), the AMQP connection

*Publisher* has the definition and the protocol between the device and the AMQP server.

```java
public class Publisher extends Thread {
    private BlockingDeque<String> queue;
    private ConnectionFactory factory;
    private static final String EXCHANGE_NAME =  "amq.topic";
    public String publisherName = "GATEWAY";
	...

    @Override
    public void run() {
        while(true) {
            try {
                Connection connection = factory.newConnection();
                Channel ch = connection.createChannel();
                ch.confirmSelect();

                while (true) {
                    String message = queue.takeFirst();
                    String sensorType = message.substring(0, message.indexOf('.'));
                    String value = message.substring(message.indexOf('.') + 1, message.length());
                    try {
                        ch.basicPublish(EXCHANGE_NAME, publisherName + '.' + sensorType, null, value.getBytes());
                        System.out.println("[s] " + value);
                        ch.waitForConfirmsOrDie();
                    } catch (Exception e){
                        System.out.println("[f] " + value);
                        queue.putFirst(message);
                        throw e;
                    }
                }
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                System.out.println("Connection broken: " + e.getClass().getName());
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

#### [ClientSocket.java](/Gateway_publisher/src/main/java/Socket/ClientSocket.java), the bridge

*Client* provides the definition and the protocol between the clients and the server and also play the role of the bridge between the Gateway and the AMQP server.

```java
class ClientSocket extends Thread {
	...
    private static void setupConnectionFactory(ConnectionFactory factory) {
        String uri = "CLOUDAMQP_URL";
        try {
            factory.setAutomaticRecoveryEnabled(false);
            factory.setUri(uri);
        } catch (KeyManagementException e1) {
            e1.printStackTrace();
        } catch (NoSuchAlgorithmException e2) {
            e2.printStackTrace();
        } catch (URISyntaxException e3) {
            e3.printStackTrace();
        }
    }

    private static void publishMessage(String message) {
        //Adds a message to internal blocking queue
        try {
            System.out.println("[q] " + message);
            queue.putLast(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        factory = new ConnectionFactory();
        setupConnectionFactory(factory);
        publishThread = new Publisher(queue, factory, "GATEWAY");
        publishThread.start();

        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());

            String clientMsg;
            while (true) {
                clientMsg = in.readUTF();
                publishMessage(clientMsg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

* Setup the connection factory *(setupConnectionFactory)*, put the message inside the queue queueing for its turn to be published *(publishMessage)*.
* Whenever, *ClientSocket* instance is created and run *(Thread)*, the thread will initiate and start *Publisher* instance and then continuously read the messages from clients and publish them to the AMQP server.

#### [Server.java](/Gateway_publisher/src/main/java/Socket/Server.java), the server

*Server* is where the server resides.

```java
public class Server extends Thread {
    private ServerSocket serverSocket;

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public void run() {
        while (true) {
            try {
                System.out.println("Waiting for client on port " +
                        serverSocket.getLocalPort() + "...");
                Socket server = serverSocket.accept();
                System.out.println("Just connected to " +
                        server.getRemoteSocketAddress());

                new ClientSocket(server).start();
            } catch (SocketTimeoutException s) {
                System.out.println("Socket timed out!");
                break;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
```

When *Server* instance is created and run, it will try to accept the connection request from the client and then start the connection between the two, then it will inititate and start the *ClientSocket* to continuously collect the data from client and forward it to the AMQP server.

#### [Main.java](/Gateway_publisher/src/main/java/Main.java), the executor

*Main* is where we put everything altogether.

```java
public class Main {
    public static void main(String[] args) {
        int port = 2000;
        try {
            new Server(port).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

## Usage

In [ClientSocket.java](/Gateway_publisher/src/main/java/Socket/ClientSocket.java), change the value of uri value to the URL can be found in the control panel for your instance.

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

You can change the publisher name, the tag of the data source, in [Publisher.java](/Gateway_publisher/src/main/java/AMQP/Publisher.java), the default is "GATEWAY".

```java
public class Publisher extends Thread {
    ...
    public String publisherName = "GATEWAY";
    ...
}
```