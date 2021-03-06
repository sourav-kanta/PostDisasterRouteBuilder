package com.example.userpc.roadtracker;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ServerTransaction extends Thread{
	
	private Socket socket=null;
	private Context context;
	private ArrayList<String> longitude;
	private ArrayList<String> latitude;
	private ArrayList<String> rec_longitude;
	private ArrayList<String> rec_latitude;
	private ArrayList<String> id;
	private ArrayList<String> date;
	private ArrayList<String> rec_id;
	private ArrayList<String> rec_date;
	
	public ServerTransaction(Socket soc,Context c)
	{
		socket=soc;
		context=c;
		longitude= new ArrayList<>();
		latitude= new ArrayList<>();
		rec_latitude= new ArrayList<>();
		rec_longitude= new ArrayList<>();
		id=new ArrayList<>();
		rec_id=new ArrayList<>();
		rec_date=new ArrayList<>();
		date=new ArrayList<>();
	}
	
	private void populateList()
	{
		DatabaseHelper helper=DatabaseHelper.getInstance(context);
		SQLiteDatabase db=helper.getUsableDataBase();
		String sql="select * from `location`;";
		Cursor cur=db.rawQuery(sql, null);
		if(cur==null)
			return;
		if(cur.moveToFirst())
		{
			do
			{
				String temp_long=cur.getString(cur.getColumnIndex("longitude"));
				String temp_lat=cur.getString(cur.getColumnIndex("latitude"));
				String temp_id=cur.getString(cur.getColumnIndex("id"));
				String temp_date=cur.getString(cur.getColumnIndex("TIME"));
				Log.e("Location", "Long = "+temp_long+" Lat = "+temp_lat);
				longitude.add(temp_long);
				latitude.add(temp_lat);
				id.add(temp_id);
				date.add(temp_date);
			}
			while(cur.moveToNext());
			cur.close();
		}
	}
	
	private void addtoDB()
	{
		DatabaseHelper helper=DatabaseHelper.getInstance(context);
		SQLiteDatabase db=helper.getUsableDataBase();
		if(rec_longitude.size()!=rec_latitude.size())
			return;
		for(int i=0;i<rec_longitude.size();i++)
		{
			String temp_longitude=rec_longitude.get(i);
			String temp_latitude=rec_latitude.get(i);
			String sql="INSERT INTO `location` (`id`, `longitude`, `latitude`, `TIME`) VALUES ('"+rec_id.get(i)+"', "+temp_longitude+", "+temp_latitude+", '"+rec_date.get(i)+"');";
			try
			{
				db.execSQL(sql);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				Log.e("Sender DB", "Error in write");
			}
		}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		Log.e("Sever", "Transaction called");
		if(socket==null)
			return;
		populateList();
		
		/*
		 * Main Protocol for server-side transfer
		 */
		try
		{
			InputStream in=socket.getInputStream();
			OutputStream out=socket.getOutputStream();
			DataInputStream din=new DataInputStream(in);
			DataOutputStream dout=new DataOutputStream(out);
			String outgoing_size=String.valueOf(longitude.size());
			dout.writeUTF(outgoing_size);
			int incoming_size=Integer.parseInt(din.readUTF());
			for(int i=0;i<longitude.size();i++)
			{
				dout.writeUTF(id.get(i));
				dout.writeUTF(longitude.get(i));
				dout.writeUTF(latitude.get(i));
				dout.writeUTF(date.get(i));
			}
			for(int i=0;i<incoming_size;i++)
			{
				String temp_id=din.readUTF();
				String temp_longitude=din.readUTF();
				String temp_latitude=din.readUTF();
				String temp_date=din.readUTF();
				rec_id.add(temp_id);
				rec_latitude.add(temp_latitude);
				rec_longitude.add(temp_longitude);
				rec_date.add(temp_date);
			}
			din.close();
			dout.close();
			in.close();
			out.close();
			socket.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			Log.e("Server Error", "In main transaction block");
			return;
		}
		addtoDB();
		try {
			LogDBHelper lhelp = LogDBHelper.getLogHelperInstance(context);
			SQLiteDatabase logdb = lhelp.getUsableLogDB();
			logdb.execSQL("INSERT INTO `logs` (`time` , `message`) VALUES ('"+Misc.getTime()+"' , 'Data successfully exchanged')");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Log.e("Service","Write to LogDb failed");
		}
		Log.e("Send", "Complete");
	}

}
