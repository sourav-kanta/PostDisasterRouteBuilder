package com.example.userpc.roadtracker;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

@SuppressWarnings("deprecation")
public class UploadThread extends Thread{
	
	private ArrayList<String> latitude;
	private ArrayList<String> longitude;
	private ArrayList<String> id;
	private ArrayList<String> date;
	private Context context;
	
	public UploadThread(Context con) {
		// TODO Auto-generated constructor stub
		context=con;
		latitude= new ArrayList<>();
		longitude= new ArrayList<>();
		id=new ArrayList<>();
		date=new ArrayList<>();
	}
	
	private void populateList()
	{
		DatabaseHelper helper=DatabaseHelper.getInstance(context);
		SQLiteDatabase db=helper.getUsableDataBase();
		String sql="select * from `location`;";
		Cursor cur=db.rawQuery(sql, null);
		if(cur==null){
			ReceiveThread.cancelEngagement();
			return;
		}			
		if(cur.moveToFirst())
		{
			do
			{
				String temp_long=cur.getString(cur.getColumnIndex("longitude"));
				String temp_lat=cur.getString(cur.getColumnIndex("latitude"));
				String temp_id=cur.getString(cur.getColumnIndex("id"));;
				String temp_date=cur.getString(cur.getColumnIndex("TIME"));
				Log.e("Location", "Id = "+temp_id+" Long = "+temp_long+" Lat = "+temp_lat+" Date = "+temp_date);
				longitude.add(temp_long);
				latitude.add(temp_lat);
				id.add(temp_id);
				date.add(temp_date);
			}
			while(cur.moveToNext());
			cur.close();
		}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		populateList();
		Httpcall req=new Httpcall();
		ArrayList<NameValuePair> param=new ArrayList<NameValuePair>();
		for(int i=0;i<latitude.size();i++)
		{
			String query="INSERT INTO `location` (`id`, `latitude`, `longitude`, `date`) VALUES ('"+id.get(i)+"', '"+latitude.get(i)+"', '"+longitude.get(i)+"', '"+date.get(i)+"');";
			Log.e("Upload_Thread", query);
			param.add(new BasicNameValuePair("pass", "SubmitValues"));
			param.add(new BasicNameValuePair("query", query));
			String response=req.makeServiceCall("http://10.0.2.2/location.php/", Httpcall.POST, param);
			Log.e("Upload_Thread", response);
			param.clear();
		}
		try {
			LogDBHelper lhelp = LogDBHelper.getLogHelperInstance(context);
			SQLiteDatabase logdb = lhelp.getUsableLogDB();
			logdb.execSQL("INSERT INTO `logs` (`time` , `message`) VALUES ('"+Misc.getTime()+"' , 'Data uploaded to server')");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Log.e("Service","Write to LogDb failed");
		}
	}	
	
}
