package com.example.userpc.roadtracker;

import java.net.HttpURLConnection;
import java.net.URL;
import android.content.Context;
import android.util.Log;

public class NetworkSniffer extends Thread{
	
	private volatile boolean stop=false;
	private Context context;
	
	public NetworkSniffer(Context con)
	{
		context=con;
	}
	
	public void stopNetworkThread()
	{
		stop=true;
		interrupt();
	}
	
	private boolean checkNetworkAvailability() 
	{
		try
		{
			URL currentUrl = new URL("http://10.0.2.2/");
			HttpURLConnection connection = (HttpURLConnection) currentUrl.openConnection();
			connection.connect();
			int responseCode=connection.getResponseCode();
			if(responseCode==200)
				return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();			
		}
		return false;
	}	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		while(!stop)
		{
			try {
				Thread.sleep(20*60*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}
			boolean checkNet=checkNetworkAvailability();
			Log.e("Network Thread", "Network available : "+checkNet);
			if(checkNet==true)
			{
				UploadThread up=new UploadThread(context);
				up.start();
			}
		}
	}	
}
