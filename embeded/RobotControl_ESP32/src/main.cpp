#include <Arduino.h>
#include <WiFi.h>
#include <PubSubClient.h>
#include <ESP32Servo.h>

#include "Point.h"

#define ledR 13
#define ledG 23
#define ledB 27

#define join1 26
#define join2 25
#define join3 27
#define join4 14

#define sensor1 19
#define sensor2 18

#define J1_MIN 0
#define J1_MAX 180
#define J2_MIN 70
#define J2_MAX 180
#define J3_MIN 113
#define J3_MAX 180
#define J4_MIN 0
#define J4_MAX 180

/* change it with your ssid-password */
const char *ssid = "XXXXXXX";
const char *password = "XXXXXXX";

// const char *mqtt_server = "mqtt.eclipseprojects.io";
const char *mqtt_server = "192.168.XXXX.XXXX";
const char *mqtt_client_id = "work1";
const char *mqtt_login = "XXXXXXX";
const char *mqtt_password = "XXXXXXX";
/* topics */
#define TOPIC "work1/#"

#define TOPIC_SENSOR1 "work1/sensor1"
#define TOPIC_SENSOR2 "work1/sensor2"

/* create an instance of PubSubClient client */
WiFiClient espClient;
PubSubClient client(espClient);

Servo servo1, servo2, servo3, servo4;
boolean sensor1PastState = false;
boolean sensor2PastState = false;
boolean startProgFlag = false;
boolean stopFlag = false;

const int MAX_POINTS = 10; // Максимальное количество точек для сохранения
int savedPointCount = 0;   // количество сохранённых точек
Point points[MAX_POINTS];  // Массив для хранения точек
Point currentPoint;

void GoToPoint(Point point)
{
  servo1.write(point.j1);
  servo2.write(point.j2);
  servo3.write(point.j3);
  servo4.write(point.j4);
}

void PrintPoint(Point point)
{
  Serial.print("J1 = " + String(point.j1));
  Serial.print("\tJ2 = " + String(point.j2));
  Serial.print("\tJ3 = " + String(point.j3));
  Serial.println("\tJ4 = " + String(point.j4));
}

void move()
{
  for (int i = 0; (i < savedPointCount) && (!stopFlag); i++)
  {
    Serial.print("Point:\t");
    Serial.println(i);
    PrintPoint(points[i]);
    GoToPoint(points[i]);
    delay(points[i].time);
  }
}

void receivedCallback(char *topic, byte *payload, unsigned int length)
{
  String topicStr(topic);
  char *payloadCh = (char *)payload;
  payloadCh[length] = 0;
  // for (int i = 0; i < length; i++)
  //   payloadCh[i] = (char)payload[i];
  String payloadStr(payloadCh);

  Serial.print("Mes:\t");
  Serial.print(topic);
  Serial.print("\t");
  Serial.println(payloadStr);

  if (topicStr.indexOf("join/1") >= 1)
  {
    int pos = payloadStr.toInt();
    currentPoint.j1 = (int)map(pos, 0, 100, J1_MIN, J1_MAX);
    servo1.write(currentPoint.j1);
  }
  else if (topicStr.indexOf("join/2") >= 1)
  {
    int pos = payloadStr.toInt();
    currentPoint.j2 = (int)map(pos, 0, 100, J2_MIN, J2_MAX);
    servo2.write(currentPoint.j2);
  }
  else if (topicStr.indexOf("join/3") >= 1)
  {
    int pos = payloadStr.toInt();
    currentPoint.j3 = (int)map(pos, 0, 100, J3_MIN, J3_MAX);
    servo3.write(currentPoint.j3);
  }
  else if (topicStr.indexOf("join/4") >= 1)
  {
    int pos = payloadStr.toInt();
    currentPoint.j4 = (int)map(pos, 0, 100, J4_MIN, J4_MAX);
    servo4.write(currentPoint.j4);
  }
  else if (topicStr.indexOf("mode") >= 1)
  {
    digitalWrite(ledR, HIGH);
    delay(1000);
    digitalWrite(ledR, LOW);
    if (payloadStr.indexOf("set") >= 0)
    {
      digitalWrite(ledB, 1);
      for (int pos = 0; pos <= 180; pos += 1)
      {
        servo1.write(pos);
        delay(15);
      }
      for (int pos = 180; pos >= 0; pos -= 1)
      {
        servo1.write(pos);
        delay(15);
      }
      digitalWrite(ledB, 0);
    }
    else if (payloadStr.indexOf("reset") >= 0)
    {
      // TODO reset handler
    }
    else if (payloadStr.indexOf("stop") >= 0)
    {
      stopFlag = true;
    }
    else if (payloadStr.indexOf("start") >= 0)
    {
      startProgFlag = true;
    }
    else if (payloadStr.indexOf("save") >= 0)
    {
      if (savedPointCount <= MAX_POINTS)
      {
        points[savedPointCount] = currentPoint;
        Serial.println("Save point " + String(savedPointCount));
        PrintPoint(points[savedPointCount++]);
      }
      else
      {
        Serial.println("Point limit");
      }
    }
    else if (payloadStr.indexOf("clear") >= 0)
    {
      savedPointCount = 0;
    }
    else if (payloadStr.indexOf("back") >= 0)
    {
      GoToPoint(points[savedPointCount - 1]);
    }
  }
}

void mqttconnect()
{
  /* Loop until reconnected */
  while (!client.connected())
  {
    Serial.print("MQTT connecting ...");
    if (client.connect(mqtt_client_id, mqtt_login, mqtt_password))
    {
      Serial.println("connected");
      /* subscribe topic with default QoS 0*/
      client.subscribe(TOPIC);
      digitalWrite(ledB, 0);
    }
    else
    {
      Serial.print("failed, status code =");
      Serial.print(client.state());
      Serial.println("try again in 5 seconds");
      /* Wait 5 seconds before retrying */
      delay(5000);
    }
  }
}

void setup()
{
  Serial.begin(115200);
  pinMode(ledR, OUTPUT);
  pinMode(ledG, OUTPUT);
  pinMode(ledB, OUTPUT);

  pinMode(sensor1, INPUT);
  pinMode(sensor2, INPUT);

  servo1.attach(join1);
  servo2.attach(join2);
  servo3.attach(join3);
  servo4.attach(join4);

  // We start by connecting to a WiFi network
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);
  digitalWrite(ledG, 1);
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED)
  {
    delay(500);
    Serial.print(".");
  }
  digitalWrite(ledG, 0);

  /* set led as output to control led on-off */

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
  digitalWrite(ledB, 1);

  /* configure the MQTT server with IPaddress and port */
  client.setServer(mqtt_server, 1883);
  /* this receivedCallback function will be invoked
  when client received subscribed topic */
  client.setCallback(receivedCallback);
}

void loop()
{
  /* if client was disconnected then try to reconnect again */
  if (!client.connected())
  {
    mqttconnect();
  }
  client.loop();

  boolean sensor1State = digitalRead(sensor1);
  boolean sensor2State = digitalRead(sensor2);
  if (sensor1State != sensor1PastState)
  {
    client.publish(TOPIC_SENSOR1, sensor1State ? "1" : "0");
    startProgFlag = sensor1State; // запустить движение по датчику
    // Serial.println("sensor1 = "+sensor1State?"1":"0");
  }
  if (sensor2State != sensor2PastState)
  {
    client.publish(TOPIC_SENSOR2, sensor2State ? "1" : "0");
    // Serial.println("sensor2 = "+sensor2State?"1":"0");
  }
  sensor1PastState = sensor1State;
  sensor2PastState = sensor2State;

  if (startProgFlag)
  {
    Serial.println("Start move");
    move();
    startProgFlag = false;
    Serial.println("End move");
  }
}
