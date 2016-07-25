package com.example.userpc.roadtracker;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;


public class GPSThread extends Thread {

    private Context context;
    private volatile boolean stop = true;
    private volatile Location location;
	String userid;
    private LocationManager locationManager;
	private LocationListener loclis;
    private final static long dist=10;     //change
    private final static long thread_delay=1*60*1000;		//change
    
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public GPSThread(Context c) {
        context = c;
		userid=MainActivity.getUserName();
    }
    
    public boolean isRunning()
    {
    	return !stop;
    }

    public void stopThread() {    	
        stop = false;
        interrupt();
    }

    @Override
    public void run() {
        super.run(); 
        Log.e("GPS", "Thread started");
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		Log.e("GPS", "Enabled "+isGPSEnabled);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, dist, loclis = new LocationListener() {

			@Override
			public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProviderEnabled(String arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProviderDisabled(String arg0) {
				// TODO Auto-generated method stub
				Log.e("GPS", "disabled");
			}

			@Override
			public void onLocationChanged(Location arg0) {
				// TODO Auto-generated method stub
				location=arg0;
				//location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				//Log.e("Location", location.toString()+"hello");
				if(location!=null)
				{

					// TODO write location to Database
					double latitude=round(location.getLatitude(),4);
					double longitude=round(location.getLongitude(),4);
					Log.e("Location", "Latitude = "+latitude+" Longitude = "+longitude);
	            	/*
	            	 * Implement write to database
	            	 * Consider making it Thread safe
	            	 * One helper object and one DB connection
	            	 * Use singleton pattern
	            	 * Shit havn't yet figured it out
	            	 * Got it motherfucker
	            	 */
					DatabaseHelper dathelp=DatabaseHelper.getInstance(context);
					SQLiteDatabase db=dathelp.getUsableDataBase();
					try{
						db.execSQL("INSERT INTO `location` (`id`, `longitude`, `latitude`) VALUES ("+userid+", "+longitude+", "+latitude+");");
						Log.e("Database", "Inserted");
						try {
							LogDBHelper lhelp = LogDBHelper.getLogHelperInstance(context);
							SQLiteDatabase logdb = lhelp.getUsableLogDB();
							logdb.execSQL("INSERT INTO `logs` (`time` , `message`) VALUES ('"+Misc.getTime()+"' , 'Locations received : "+latitude+" , "+longitude+"')");
						}
						catch (Exception e)
						{
							e.printStackTrace();
							Log.e("Service","Write to LogDb failed");
						}
					}
					catch(SQLiteConstraintException e){
						e.printStackTrace();
						Log.e("Database", "Same Values");
					}
				}
				else
				{
					Log.e("GPS", "Unknown location");
				}
			}
		}, Looper.getMainLooper());
        while (stop) {               
            try {
				Thread.sleep(thread_delay);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}            
        }
		locationManager.removeUpdates(loclis);
    	Log.e("GPS", "Thread stopped");
    }
}
