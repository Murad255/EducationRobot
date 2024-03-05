#include <Arduino.h>
#pragma once

class Point
{
public:
	int j1, j2, j3, j4, j5;
	int time = 1000; // ms

	Point(int j1, int j2, int j3, int j4, int j5, int time)
	{
		this->j1 = j1;
		this->j2 = j2;
		this->j3 = j3;
		this->j4 = j4;
		this->j5 = j5;
		this->time = time;
	}

	Point() {}
	String toStrintg()
	{
		return "Point(" + String(j1) + "," + String(j2) + "," + String(j3) + "," + String(j4) + "," + String(j5) + "," + String(time) + ")";
	}
};