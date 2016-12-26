package com.example.userpc.roadtracker;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Date;

public class DumpDatabase extends AsyncTask<Void,Void,Void>{

    private final Context context;

    public DumpDatabase(Context con)
    {
        context=con;
    }

    @Override
    protected void onPreExecute() {
        File folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + "DB_DUMP");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
        if (success) {
            Log.e("Database","Dump folder created");
        } else {
            Log.e("Database","Dump folder cant be created.");
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try
        {
            DatabaseHelper helper=DatabaseHelper.getInstance(context);
            SQLiteDatabase db=helper.getUsableDataBase();
            String sql="select * from `location`;";
            JSONArray jsonArray=new JSONArray();
            Cursor cur=db.rawQuery(sql, null);
            if(cur==null)
                return null;
            if(cur.moveToFirst())
            {
                do
                {
                    String temp_long=cur.getString(cur.getColumnIndex("longitude"));
                    String temp_lat=cur.getString(cur.getColumnIndex("latitude"));
                    String temp_id=cur.getString(cur.getColumnIndex("id"));
                    String temp_date=cur.getString(cur.getColumnIndex("TIME"));
                    Log.e("Location", "Long = "+temp_long+" Lat = "+temp_lat);
                    JSONObject obj=new JSONObject();
                    obj.put("id",temp_id);
                    obj.put("longitude",temp_long);
                    obj.put("latitude",temp_lat);
                    obj.put("time",temp_date);
                    jsonArray.put(obj);
                }
                while(cur.moveToNext());
                Writer output;
                File file = new File(Environment.getExternalStorageDirectory() +
                        File.separator + "DB_DUMP" + File.separator + new Date().getTime() + ".json");
                output = new BufferedWriter(new FileWriter(file));
                output.write(jsonArray.toString());
                output.close();
                cur.close();
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Log.e("Database","Dumped");
        Toast.makeText(context,"Database Dumped",Toast.LENGTH_SHORT).show();
        try {
            LogDBHelper lhelp = LogDBHelper.getLogHelperInstance(context);
            SQLiteDatabase logdb = lhelp.getUsableLogDB();
            logdb.execSQL("INSERT INTO `logs` (`time` , `message`) VALUES ('"+Misc.getTime()+"' , 'Data successfully dumped')");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.e("Service","Write to LogDb failed");
        }
    }
}
