Mobile-Sensors
==============
Computer Networks | CC14KHMT | 2017/02/27 - 2017/03/03 | Bach Khoa University (BKU)

The Concept
-----------

A lot of applications across different disciplines involved the usage of sensors. We want to provide an economical way to retrieve data from environment using sensors on smartphones.

Getting Started
---------------

The repository was completely built upon Java language and consists of the 3 phases:
1. Retrieve sensors data from Android devices *(clients)* sensors using ![sensorUp](https://lh3.googleusercontent.com/5-15CZICWn_ZjN49yVTfgdzFquey4YC7ZgrXESPzT0HehcBok7Qod1G0CPUxJ9mwZFpX6A38=s30 "ic_launcher.png") **sensorUp** app.
2. Upload your sensors data:
	* To a gateway *(server)* by establishing [TCP](https://en.wikipedia.org/wiki/Transmission_Control_Protocol) connection [(Socket)](https://en.wikipedia.org/wiki/Network_socket) between the two.
		* 	The data will be automatically stored and forwarded to the AMQP server.
	* To an Advanced Message Queuing Protocol [(AMQP)](https://en.wikipedia.org/wiki/Advanced_Message_Queuing_Protocol) server *(publishers)*.
		*	From Gateway or from ![sensorUp](https://lh3.googleusercontent.com/5-15CZICWn_ZjN49yVTfgdzFquey4YC7ZgrXESPzT0HehcBok7Qod1G0CPUxJ9mwZFpX6A38=s30 "ic_launcher.png") **sensorUp** app.
		*	We are using [RabbitMQ](http://www.rabbitmq.com/).
3. Getting data from AMQP server *(subscribers)* and do some analytics using ![graphDown](https://lh3.googleusercontent.com/h8CLPE0NB7A-0c09zfubPISOpcsolLwTOYSUgGA6ALCQdbeFz-gVOoRPVXvuhcop7v63XtzQ=s30 "ic_launcher.png") **graphDown** app.

Features
--------

### 1. sensorUp, the Android app
* Upload sensors data to Gateway as a client or directly to AMQP server as a publisher.
* Automatically switch to uploading to AMQP server when the Gateway becomes unavailable.
* Allow the specification of the host and port for the Gateway.
* Allow the specification of the publisher name as a source tag for data available to the subscribers.
* Allow the preference of which type of sensors the app will collect the data from.
	* Pressure
	* Temperature
	* Light
* Display uploaded sensors' data in a list.

### 2. Gateway
* Support up to a couple of tenth of the number of clients.
* Gather sensor data transferred from clients.
* Filter data based on sensor type and publisher name.
* Publish data as a publisher to AMQP server.
* Log the published data.
 
### 3. AMQP server
* Has features that is available to [cloudAMQP](https://www.cloudamqp.com/)'s [Little Lemur](https://www.cloudamqp.com/plans.html) plan customers.
	* Max 1M messages per month
	* Max 20 concurrent connections
	* Max 100 queues
	* Max 10 000 queued messages
	* Max idle queue time 28 days
* Distribute sensor data to corresponding subsribers based on sensor type and publisher name.
 
### 4. graphDown, another Android app
* Read data from AMQP server based on sensor type and publisher name.
* Graph the sensor data of corresponding type.
* Display graphed sensor data in a list.

Authors
-------

* [Pham Huynh Son](mailto:sonpham1996@gmail.com)
* [Le Quynh Anh](mailto:suice.san@gmail.com)

Usage
-----

* You are free to use our work for **educational** purposes and any **non-commercial** projects.
* For the detail on these modules, please have a look at the following links:
	*	[sensorUp readme](/Sensors_publisher/README.md)
	*	[The Gateway readme](/Gateway_publisher/README.md)
	*	[graphDown readme](/AMQP_subscriber/README.md)
