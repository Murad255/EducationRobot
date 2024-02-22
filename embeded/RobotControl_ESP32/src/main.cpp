#include <Arduino.h>

#define ledR 13
#define ledG 25
#define ledB 27

#define join1 14
#define join2 4
#define join3 15
#define join4 26

#include <WiFi.h>
#include <PubSubClient.h>
#include <ESP32Servo.h>

/* change it with your ssid-password */
const char *ssid = "xxxxxxx";
const char *password = "xxxxxxx";

// const char *mqtt_server = "mqtt.eclipseprojects.io";
const char *mqtt_server = "xxxxxxxxxx";
const char *mqtt_client_id = "work1";
const char *mqtt_login = "xxxxxxx";
const char *mqtt_password = "xxxxxxxxxx";
/* topics */
#define LED_TOPIC "work1/#"

/* create an instance of PubSubClient client */
WiFiClient espClient;
PubSubClient client(espClient);

Servo servo1, servo2, servo3, servo4;

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
    servo1.write(map(pos, 0, 100, 0, 180));
  }
  else if (topicStr.indexOf("join/2") >= 1)
  {
    int pos = payloadStr.toInt();
    servo2.write(map(pos, 0, 100, 0, 180));
  }
  else if (topicStr.indexOf("join/3") >= 1)
  {
    int pos = payloadStr.toInt();
    servo3.write(map(pos, 0, 100, 0, 180));
  }
  else if (topicStr.indexOf("join/4") >= 1)
  {
    int pos = payloadStr.toInt();
    servo4.write(map(pos, 0, 100, 0, 180));
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
      // TODO stop handler
    }
    else if (payloadStr.indexOf("start") >= 0)
    {
      // TODO start handler
    }
    else if (payloadStr.indexOf("save") >= 0)
    {
      // TODO save handler
    }
    else if (payloadStr.indexOf("clear") >= 0)
    {
      // TODO clear handler
    }
    else if (payloadStr.indexOf("back") >= 0)
    {
      // TODO back handler
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
      client.subscribe(LED_TOPIC);
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
}
