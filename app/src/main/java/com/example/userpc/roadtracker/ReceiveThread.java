package com.example.userpc.roadtracker;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * Created by USER PC on 5/21/2016.
 */
public class ReceiveThread extends Thread{
	
	private Context context;
	private volatile boolean stopThread=true;
	private static volatile boolean engage=true;
	ArrayList<String> blacklist=null;
	
	public void stopReceiverThread()
	{
		stopThread=false;
		interrupt();
	}

	public ReceiveThread(Context c) {
		// TODO Auto-generated constructor stub
		context=c;
		stopThread=true;
		engage=true;
		WManager manage=new WManager();
		if(!manage.isWifiOn(context))
		{
			manage.enableWifi(context);
		}	
		blacklist= new ArrayList<>();
	}
	
	public static void cancelEngagement()
	{
		engage=true;
	}
	
	private boolean connectToServer(Context context)
	{
		WifiConfiguration conf = new WifiConfiguration();		
		conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);		
		WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE); 	
		wifiManager.disconnect();
		List<ScanResult> list =  wifiManager.getScanResults();
		if(list==null)
		{
			cancelEngagement();
			return false;
		}			
		for( ScanResult i : list ) {
			Log.e("SSID", i.SSID);
		    if(i.SSID != null && (i.SSID.startsWith("SMSKCM877-") && i.SSID.endsWith("-MSDBP2016") && (!blacklist.contains(i.SSID)))) {
		    	Log.e("SSID Connected", i.SSID);
		    	 conf.SSID="\""+i.SSID+"\"";
		    	 conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		    	 wifiManager.addNetwork(conf);
		    	 int networkID=wifiManager.addNetwork(conf);
		         wifiManager.disconnect();		         
		         wifiManager.enableNetwork(networkID, true);
		         wifiManager.reconnect(); 
		         ReceiverTransaction rec=new ReceiverTransaction(context);
		         rec.start();
		         blacklist.add(i.SSID);
		         return true;
		    }           
		 }
		cancelEngagement();
		return false;
	}
	
    @Override
    public void run() {
        super.run();
        while(stopThread)
        {
        	//MAKE sure time delay between mode switch is more than or equal to 2 minutes
        	
        	try {
				Thread.sleep((long) (0.3*60*1000));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}
        	
        	if(engage)
        	{
        		engage=false;
        		boolean connected=connectToServer(context);
        		Log.e("Receiver", "Connected "+connected);
        	}        	
        }
        WManager manage=new WManager();
        if(manage.isWifiOn(context))
        {
        	manage.disableWifi(context);
        }
        cancelEngagement();
    }
}
