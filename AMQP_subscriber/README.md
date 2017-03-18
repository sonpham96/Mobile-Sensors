# GraphDown ![graphDown](https://lh3.googleusercontent.com/h8CLPE0NB7A-0c09zfubPISOpcsolLwTOYSUgGA6ALCQdbeFz-gVOoRPVXvuhcop7v63XtzQ=s30 "ic_launcher.png")

A graphical illustration for your mobile sensor data

### Introduction

The aim of this app is to keep track of another phone's ( the publisher) environmental information (temperature, light, pressure) and visualize with live graph.

### Prerequisites

This app runs on Android devices 4.3+ and requires Internet connection.

### Demo
First, enter the publisher name. Then choose one of three type of sensor (light, pressure and temperature).
Note that if you want to subscribe to all publisher, enter '*' in publisher name .

![graphdown_1](https://cloud.githubusercontent.com/assets/26101199/24070310/67510506-0bed-11e7-9e8b-6114bae371c9.png)

Below is a demonstration of light sensor data from "Android" publisher. The graph illustrate the past 100 data point while the log below show the exact value of recent data point as well as its time.

![graphdown_2](https://cloud.githubusercontent.com/assets/26101199/24070582/f4ff76bc-0bf2-11e7-8fb2-79d9b96de933.png)

### A brief description of implementation

[LauchActivity](https://github.com/sonpham96/Mobile-Sensors/blob/master/AMQP_subscriber/app/src/main/java/com/sonpham/amqp_subscriber/LaunchActivity.java)
Set up basic information including publisher name and the type of data you want to see.
[MainActivity](https://github.com/sonpham96/Mobile-Sensors/blob/master/AMQP_subscriber/app/src/main/java/com/sonpham/amqp_subscriber/MainActivity.java)
Set up the connection.
Start subscriber thread to get data from cloud AMQP and display it on screen.
[Subscriber](https://github.com/sonpham96/Mobile-Sensors/blob/master/AMQP_subscriber/app/src/main/java/com/sonpham/amqp_subscriber/Subscriber.java)


## Built With

* [CloudAMQP](https://www.cloudamqp.com/) - Service managed RabbitMQ servers in the cloud
* [GraphView](http://www.android-graphview.org/) - Used to generate live graph


