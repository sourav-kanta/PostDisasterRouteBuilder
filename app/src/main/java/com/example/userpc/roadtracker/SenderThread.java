package com.example.userpc.roadtracker;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.util.Log;

/**
 * Created by USER PC on 5/21/2016.
 */
public class SenderThread extends Thread{
	
	Context context=null;
	private volatile boolean stopThread=true;
	private final static int PORT=4444;
	private ServerSocket server;
	
	public SenderThread(Context c) {
		// TODO Auto-generated constructor stub
		context=c;	
		initServer();
	}
	
	public void stopThread()
	{
		stopThread=false;
		interrupt();
		try {
			server.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void initServer()
	{
		try {
			server=new ServerSocket(PORT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ApManager access=new ApManager();
		WifiConfiguration hots=access.setHotspotName(Misc.getHotName(), context);
		access.ApStateOn(context, hots);
	}

    @Override
    public void run() {
        super.run();
        while(stopThread)
        {
        	Log.e("Server", "Listening");
        	try {
				Socket socket=server.accept();
				ServerTransaction send=new ServerTransaction(socket,context);
				send.start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e("Server", "Problem in acccept block or forcefull exit");
			}
        }
        try {
			server.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("Server", "cannot be closed");
		}
        ApManager hotspot=new ApManager();
        if(hotspot.isApOn(context))
        	hotspot.ApStateOff(context);
    }
}
