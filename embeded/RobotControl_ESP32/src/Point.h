#include <Arduino.h>
#pragma once

class Point
{
public:
	int j1, j2, j3, j4;
	int time = 1000; // ms

	Point(int j1, int j2, int j3, int j4)
	{
		this->j1 = j1;
		this->j2 = j2;
		this->j3 = j3;
		this->j4 = j4;
	}

	Point() {}

	void equal(Point point)
	{
		j1 = point.j1;
		j2 = point.j2;
		j3 = point.j3;
		j4 = point.j4;
	}
};