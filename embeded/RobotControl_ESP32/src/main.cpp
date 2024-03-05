#include <Arduino.h>
#include <WiFi.h>
#include <PubSubClient.h>
#include <ESP32Servo.h>
#include <DHT.h>
#include <Adafruit_Sensor.h>
#include "Point.h"

#define ledR 13
#define ledG 23
#define ledB 27

#define join1 26
#define join2 25
#define join3 27
#define join4 14
#define join5 12

#define sensor1 19
#define sensor2 18
#define DHTPin 4

#define J1_MIN 0
#define J1_MAX 180
#define J2_MIN 70
#define J2_MAX 180
#define J3_MIN 113
#define J3_MAX 180
#define J4_MIN 0
#define J4_MAX 180
#define J5_MIN 0
#define J5_MAX 180

#define J1_START 90
#define J2_START 120
#define J3_START 170
#define J4_START 90
#define J5_START 90

/* change it with your ssid-password */
const char *ssid = "ХХХХХХХХХ";
const char *password = "ХХХХХХХХХ";

// const char *mqtt_server = "mqtt.eclipseprojects.io";
const char *mqtt_server = "192.168.ХХХХХХХХХ";
const char *mqtt_client_id = "work1";
const char *mqtt_login = "ХХХХХХХХХ";
const char *mqtt_password = "ХХХХХХХХХ";
/* topics */
#define TOPIC "work1/#"

#define TOPIC_SENSOR1 "work1/sensor1"
#define TOPIC_SENSOR2 "work1/sensor2"
#define TOPIC_SENSOR_TEMP "work1/temp"
#define TOPIC_SENSOR_HUMP "work1/hump"

/* create an instance of PubSubClient client */
WiFiClient espClient;
PubSubClient client(espClient);

Servo servo1, servo2, servo3, servo4, servo5;
boolean sensor1PastState = false;
boolean sensor2PastState = false;
boolean startProgFlag = false;
boolean stopFlag = false;

const int MAX_POINTS = 10; // Максимальное количество точек для сохранения
int savedPointCount = 0;   // количество сохранённых точек
Point points[MAX_POINTS];  // Массив для хранения точек
Point currentPoint(J1_START, J2_START, J3_START, J4_START, J5_START, 1000);
const int stepsCount = 20;

long pastSentTime = 0;
DHT dht(DHTPin, DHT11); // Инициализируем DHT

void GoToPoint(Point point)
{
  servo1.write(point.j1);
  servo2.write(point.j2);
  servo3.write(point.j3);
  servo4.write(point.j4);
  servo5.write(point.j5);
}

void GoToPTP(Point pointStart, Point pointEnd)
{
  for (int i = 1; i <= stepsCount; i++)
  {
    Point tempPoint;
    tempPoint.j1 = pointStart.j1 + (pointEnd.j1 - pointStart.j1) * i / stepsCount;
    tempPoint.j2 = pointStart.j2 + (pointEnd.j2 - pointStart.j2) * i / stepsCount;
    tempPoint.j3 = pointStart.j3 + (pointEnd.j3 - pointStart.j3) * i / stepsCount;
    tempPoint.j4 = pointStart.j4 + (pointEnd.j4 - pointStart.j4) * i / stepsCount;
    tempPoint.j5 = pointStart.j5 + (pointEnd.j5 - pointStart.j5) * i / stepsCount;
    GoToPoint(tempPoint);
    delay(pointEnd.time / stepsCount);
  }
}

void PrintPoint(Point point)
{
  Serial.print("J1 = " + String(point.j1));
  Serial.print("\tJ2 = " + String(point.j2));
  Serial.print("\tJ3 = " + String(point.j3));
  Serial.println("\tJ4 = " + String(point.j4));
  Serial.println("\tJ5 = " + String(point.j5));
}

void move()
{
  if (savedPointCount == 0)
    return;

  GoToPTP(currentPoint, points[0]);
  Serial.println(points[0].toStrintg());
  for (int i = 1; (i < savedPointCount) && (!stopFlag); i++)
  {
    GoToPTP(points[i - 1], points[i]);
    Serial.println(points[i].toStrintg());
  }
  currentPoint = points[savedPointCount - 1];
}

void move1()
{
  Serial.println("Start move1");
  Point move1Points[] = {
      Point(0, 0, 0, 0, 0, 1000),
      Point(0, 0, 0, 0, 0, 1000)};
  int pointsSize = 2;

  if (pointsSize == 0)
    return;

  GoToPTP(currentPoint, move1Points[0]);
  for (int i = 1; (i < pointsSize) && (!stopFlag); i++)
  {
    GoToPTP(move1Points[i - 1], move1Points[i]);
  }
  currentPoint = points[pointsSize - 1];
  Serial.println("End move1");
}

void move2()
{
  Serial.println("End move2");
  Point move2Points[] = {
      Point(0, 0, 0, 0, 0, 1000),
      Point(0, 0, 0, 0, 0, 1000)};
  int pointsSize = 2;

  if (pointsSize == 0)
    return;

  GoToPTP(currentPoint, move2Points[0]);
  for (int i = 1; (i < pointsSize) && (!stopFlag); i++)
  {
    GoToPTP(move2Points[i - 1], move2Points[i]);
  }
  currentPoint = points[pointsSize - 1];
  Serial.println("End move2");
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
    currentPoint.j1 = min(max(pos, J1_MIN), J1_MAX); //(int)map(pos, 0, 100, J1_MIN, J1_MAX);
    servo1.write(currentPoint.j1);
  }
  else if (topicStr.indexOf("join/2") >= 1)
  {
    int pos = payloadStr.toInt();
    currentPoint.j2 = min(max(pos, J2_MIN), J2_MAX); //(int)map(pos, 0, 100, J2_MIN, J2_MAX);
    servo2.write(currentPoint.j2);
  }
  else if (topicStr.indexOf("join/3") >= 1)
  {
    int pos = payloadStr.toInt();
    currentPoint.j3 = min(max(pos, J3_MIN), J3_MAX); //(int)map(pos, 0, 100, J3_MIN, J3_MAX);
    servo3.write(currentPoint.j3);
  }
  else if (topicStr.indexOf("join/4") >= 1)
  {
    int pos = payloadStr.toInt();
    currentPoint.j4 = min(max(pos, J4_MIN), J4_MAX);
    ; //(int)map(pos, 0, 100, J4_MIN, J4_MAX);
    servo4.write(currentPoint.j4);
  }
  else if (topicStr.indexOf("join/5") >= 1)
  {
    int pos = payloadStr.toInt();
    currentPoint.j5 = min(max(pos, J5_MIN), J5_MAX); // (int)map(pos, 0, 270, J5_MIN, J5_MAX);
    servo4.write(currentPoint.j5);
  }
  else if (topicStr.indexOf("time") >= 1)
  {
    currentPoint.time = payloadStr.toInt();
  }
  else if (topicStr.indexOf("mode") >= 1)
  {
    digitalWrite(ledR, HIGH);
    delay(1000);
    digitalWrite(ledR, LOW);
    if (payloadStr.indexOf("set") >= 0)
    {
      currentPoint.j5 = 40;
      servo4.write(currentPoint.j5);
    }
    else if (payloadStr.indexOf("reset") >= 0)
    {
      currentPoint.j5 = J5_MAX;
      servo4.write(currentPoint.j5);
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

/////////////////////////////////////SETUP////////////////////////////////////////
void setup()
{
  Serial.begin(115200);

  pinMode(sensor1, INPUT);
  pinMode(sensor2, INPUT);
  pinMode(DHTPin, INPUT);

  servo1.attach(join1);
  servo2.attach(join2);
  servo3.attach(join3);
  servo4.attach(join4);
  servo5.attach(join5);

  // We start by connecting to a WiFi network
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED)
  {
    delay(500);
    Serial.print(".");
  }

  /* set led as output to control led on-off */

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
  dht.begin();

  /* configure the MQTT server with IPaddress and port */
  client.setServer(mqtt_server, 1883);
  /* this receivedCallback function will be invoked
  when client received subscribed topic */
  client.setCallback(receivedCallback);
  GoToPoint(currentPoint);
}

/////////////////////////////////////LOOP////////////////////////////////////////
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
    // запустить движение по 1 датчику
    // if (sensor1State == true)
    //   move1();
  }
  if (sensor2State != sensor2PastState)
  {
    client.publish(TOPIC_SENSOR2, sensor2State ? "1" : "0");
    // запустить движение по датчику
    // if (sensor2State == true)
    //   move2();
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

  long currentTime = millis();
  if (currentTime - pastSentTime >= 1000)
  {
    pastSentTime = currentTime;
    float temp = dht.readTemperature();
    float hum = dht.readHumidity();

    client.publish(TOPIC_SENSOR_TEMP, String(temp).c_str());
    client.publish(TOPIC_SENSOR_HUMP, String(hum).c_str());
  }
}
