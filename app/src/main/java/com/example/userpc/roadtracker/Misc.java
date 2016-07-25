package com.example.userpc.roadtracker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Misc {

	public static String getHotName()
	{
		String name="SMSKCM877-"+((int)(Math.random() * ((99999) + 1)))+"-MSDBP2016";
		return name;
	}

	public static String getTime()
	{
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		Date dt = new Date();
		String time = sdf.format(dt);
		return time;
	}
}
