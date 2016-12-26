package com.example.userpc.roadtracker;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity {

	int notificationId=12345;
	private static String userid;

	public static String getUserName()
	{
		return  userid;
	}

	public void showmap(View v)
	{
		Intent i=new Intent(this,Show_Map.class);
		startActivity(i);
	}

	public void startMainService(View v)
	{
		if(MainServiceThread.isRunning())
			return;
		Intent service=new Intent(this,MainServiceThread.class);
		startService(service);
		Log.e("Sevice", "initialted");
		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(this)
				.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
				.setSmallIcon(R.drawable.ic_launcher)
				.setOngoing(true)
				.setContentTitle("Route Builder")
				.setContentText("Tracking your location.");
		NotificationManager mNotificationManager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(notificationId, mBuilder.build());
		//finish();
	}

	public void stopMainService(View v)
	{
		if(!MainServiceThread.isRunning())
			return;
		NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		manager.cancel(notificationId);
		MainServiceThread.stopServiceThread();
	}

	private void upload()
	{

		ArrayList<String> longitude=new ArrayList<String>();
		ArrayList<String> latitude=new ArrayList<String>();
		DatabaseHelper helper=DatabaseHelper.getInstance(this);
		SQLiteDatabase db=helper.getUsableDataBase();
		String sql="select * from `location`;";
		Cursor cur=db.rawQuery(sql, null);
		if(cur==null){
			return;
		}			
		if(cur.moveToFirst())
		{
			do
			{
				String temp_long=cur.getString(cur.getColumnIndex("longitude"));
				String temp_lat=cur.getString(cur.getColumnIndex("latitude"));
				Log.e("Time",cur.getString(cur.getColumnIndex("TIME")));
				Log.e("ID",cur.getString(cur.getColumnIndex("id")));
				longitude.add(temp_long);
				latitude.add(temp_lat);
			}
			while(cur.moveToNext());
			cur.close();
		}



		String message="";
		for(int i=0;i<longitude.size();i++)
		{
			message+=longitude.get(i)+" "+latitude.get(i)+"\n";
		}

		AlertDialog.Builder build=new AlertDialog.Builder(this);
		build.setMessage(message);
		build.setTitle("Your Locations");
		AlertDialog alert=build.create();
		alert.setCancelable(true);
		alert.show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		SharedPreferences editor=getSharedPreferences("RoadPref",MODE_PRIVATE);
		userid=editor.getString("id","0");
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		getMenuInflater().inflate(R.menu.main,menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		int id=item.getItemId();
		switch(id)
		{
			case R.id.loc : upload();
				return true ;
			case R.id.log : Intent i=new Intent(this,LogFrag.class);
				startActivity(i);
				return true;
			case R.id.dump:
				new DumpDatabase(MainActivity.this).execute();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
}
