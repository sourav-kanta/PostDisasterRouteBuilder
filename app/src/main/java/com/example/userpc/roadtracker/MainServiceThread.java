package com.example.userpc.roadtracker;


import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import java.sql.Date;
import java.sql.SQLException;

/**
 * Created by USER PC on 5/21/2016.
 */
public class MainServiceThread extends Service {

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private volatile static boolean stopService=true;
    private static HandlerThread mThread;
    private static boolean inBusiness=false;
    
    public static void stopServiceThread()
    {
    	stopService=false;
    	mThread.interrupt();
    }
    
    public static boolean isRunning()
    {
    	return inBusiness;
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
        	Log.e("Service", "looper started");
            try {
                LogDBHelper lhelp = LogDBHelper.getLogHelperInstance(getApplicationContext());
                SQLiteDatabase logdb = lhelp.getUsableLogDB();
                logdb.execSQL("INSERT INTO `logs` (`time` , `message`) VALUES ('"+Misc.getTime()+"' , 'Service Started')");
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.e("Service","Write to LogDb failed");
            }
        	GPSThread gp=new GPSThread(getApplicationContext());
			gp.start();			
			ModeController mc=new ModeController(getApplicationContext());
			mc.start();
			NetworkSniffer nh=new NetworkSniffer(getApplicationContext());
			nh.start();
        	while(stopService)
        	{          		    
				try {
					Thread.sleep(10*60*1000);
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
        	}    
        	mc.stopThread();
        	gp.stopThread();
        	nh.stopNetworkThread();
            try {
                LogDBHelper lhelp = LogDBHelper.getLogHelperInstance(getApplicationContext());
                SQLiteDatabase logdb = lhelp.getUsableLogDB();
                logdb.execSQL("INSERT INTO `logs` (`time` , `message`) VALUES ('"+Misc.getTime()+"' , 'Service Stopped')");
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.e("Service","Write to LogDb failed");
            }
        	stopSelf(msg.arg1);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
    	Log.e("Service", "in onCreate");
    	stopService=true;
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        mThread=thread;
        thread.start();
        mServiceLooper = thread.getLooper();
    mServiceHandler = new ServiceHandler(mServiceLooper);
}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("Service","Started");
        inBusiness=true;
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThread=null;
        inBusiness=false;
        Log.e("Service","Destroyed");
}


}
