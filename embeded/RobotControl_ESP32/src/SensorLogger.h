#pragma once
#include <Arduino.h>
#include <EEPROM.h>
#include <Preferences.h>
 #define DEBUG
namespace SensorLogger
{

	struct DeviceSetings
	{
		char Ssid[30];
		char Pass[30];
		char DeviceName[30];
		char SpaceName[30];
	};


	DeviceSetings setings;
	Preferences prefs; //энергонезависимая память
	bool sensorLoggerIsBegin = false;


	void SaveSetings();
	void dPrint(String message);
	void dPrintln(String message);

	void begin()
	{
		dPrintln("SensorLogger begin");
		prefs.begin("DeviceName", false);
		prefs.begin("SpaceName", false);
		prefs.begin("Ssid", false);
		prefs.begin("Pass", false);

		prefs.getBytes("DeviceName", setings.DeviceName, sizeof(setings.DeviceName));
		prefs.getBytes("SpaceName", setings.SpaceName, sizeof(setings.SpaceName));
		prefs.getBytes("Ssid", setings.Ssid, sizeof(setings.Ssid));
		prefs.getBytes("Pass", setings.Pass, sizeof(setings.Pass));

		prefs.end();

		dPrintln("setings:");
		dPrintln("name: " + String(setings.DeviceName));
		dPrintln("groop: " + String(setings.SpaceName));
		dPrintln("Ssid: " + String(setings.Ssid));
		dPrintln("pass: " + String(setings.Pass));
		
		sensorLoggerIsBegin = true;
	}

	void SaveSetings()
	{
		prefs.begin("DeviceName", false);
		prefs.begin("SpaceName", false);
		prefs.begin("Ssid", false);
		prefs.begin("Pass", false);

		prefs.putBytes("DeviceName", setings.DeviceName, sizeof(setings.DeviceName));
		Serial.print("Save SpaceName = ");
		Serial.println(setings.SpaceName);
		prefs.putBytes("SpaceName", setings.SpaceName, sizeof(setings.SpaceName));
		prefs.putBytes("Ssid", setings.Ssid, sizeof(setings.Ssid));
		prefs.putBytes("Pass", setings.Pass, sizeof(setings.Pass));
	
		prefs.end();

		dPrintln("Save prefs");
	}

	void dPrint(String message)
	{
#ifdef DEBUG
		Serial.print(message);
#endif
	}

	void dPrintln(String message)
	{
#ifdef DEBUG
		Serial.println(message);
#endif
	}
}
