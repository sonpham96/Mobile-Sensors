# GraphDown ![graphDown](https://lh3.googleusercontent.com/h8CLPE0NB7A-0c09zfubPISOpcsolLwTOYSUgGA6ALCQdbeFz-gVOoRPVXvuhcop7v63XtzQ=s30 "ic_launcher.png")

A graphical illustration for your mobile sensor data.

### Introduction

The aim of this app is to keep track of another phone's (the publisher) environmental information (temperature, light, pressure) and visualize with live graph.

### Prerequisites

This app runs on Android devices 4.4+ and requires Internet connection.

### Demo
First, enter the publisher name. Then choose one of three type of sensor (light, pressure and temperature).
Note that if you want to subscribe to all publisher, enter '*' in publisher name.

<img src="https://cloud.githubusercontent.com/assets/26101199/24070310/67510506-0bed-11e7-9e8b-6114bae371c9.png" width="400">

Below is a demonstration of light sensor data from "Android" publisher. The graph illustrate the past 100 data point while the log below show the exact value of recent data point as well as its time.

<img src="https://cloud.githubusercontent.com/assets/26101199/24070582/f4ff76bc-0bf2-11e7-8fb2-79d9b96de933.png" width="400">
### A brief description of implementation

#### [LauchActivity](/AMQP_subscriber/app/src/main/java/com/sonpham/amqp_subscriber/LaunchActivity.java)
Set up basic information including publisher name and the type of data you want to see.
#### [MainActivity](/AMQP_subscriber/app/src/main/java/com/sonpham/amqp_subscriber/MainActivity.java)
Set up the connection by starting subscriber thread. Getting data from subscriber thread and then display it on the screen as graph and the list graphed data.
#### [Subscriber](/AMQP_subscriber/app/src/main/java/com/sonpham/amqp_subscriber/Subscriber.java)
Establish the connection to AMQP server in background and continuously read data from the publisher.

## Built With

* [CloudAMQP](https://www.cloudamqp.com/) - Service managed RabbitMQ servers in the cloud
* [GraphView](http://www.android-graphview.org/) - Used to generate live graph
