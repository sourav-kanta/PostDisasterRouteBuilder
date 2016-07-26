package com.example.userpc.roadtracker;

import java.util.Random;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ModeController extends Thread{
	
	Context context;
	private volatile boolean stop=true;
	private static final int SEND=0;
	private static final int RECEIVE=1;
	private ReceiveThread rt=null;
	private SenderThread st=null;
	private int MODE;
	
	public ModeController(Context c) {
		// TODO Auto-generated constructor stub
		context=c;
		MODE=0;
	}
	
	public void stopThread()
	{
		stop=false;
		interrupt();
	}
	
	private void switchMode()
	{		
		Random rn = new Random();
		int range = 3;
		int nextMode =  rn.nextInt(range);
		String mode=nextMode==0?"Sending":"Receiving";
		try {
			LogDBHelper lhelp = LogDBHelper.getLogHelperInstance(context);
			SQLiteDatabase logdb = lhelp.getUsableLogDB();
			logdb.execSQL("INSERT INTO `logs` (`time` , `message`) VALUES ('"+Misc.getTime()+"' , 'Switched to "+mode+" mode')");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Log.e("Service","Write to LogDb failed");
		}
		Log.e("Random", "Number generated = "+nextMode);
		if(MODE==ModeController.SEND)
		{
			if(st!=null)
			{
				st.stopThread();
				st=null;
				if(rt!=null)
				{
					rt.stopReceiverThread();				
				}
				try {
					Thread.sleep((long) (1.5*60*1000));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
			}						
		}
		else if(MODE==ModeController.RECEIVE)
		{
			if(rt!=null)
			{
				rt.stopReceiverThread();
				rt=null;
				if(st!=null)
				{
					st.stopThread();				
				}
				try {
					Thread.sleep((long) (1.5*60*1000));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
			}					
		}
		if(nextMode==0)
		{
			st=new SenderThread(context);
			st.start();
			MODE=ModeController.SEND;
		}
		else if(nextMode==1 || nextMode==2)
		{
			rt=new ReceiveThread(context);
			rt.start();
			MODE=ModeController.RECEIVE;
		}
		Log.e("Mode", "Switched to "+MODE);
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		boolean firsttime=true;
		while(stop)
		{
			if(firsttime!=true) {
				try {
					Thread.sleep(15 * 60 * 1000);    //change
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					break;
				}
			}
			else
			{
				firsttime=false;
			}
			switchMode();
		}
		if(rt!=null)
			rt.stopReceiverThread();
		if(st!=null)
			st.stopThread();
	}
}
