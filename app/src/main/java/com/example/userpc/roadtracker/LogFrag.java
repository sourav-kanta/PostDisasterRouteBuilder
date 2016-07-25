package com.example.userpc.roadtracker;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by USER PC on 6/13/2016.
 */
public class LogFrag extends AppCompatActivity {

    Context context=this;

    public void updateLog(View v)
    {
        TextView t= (TextView) findViewById(R.id.log_text);
        showLog(t);
        Toast.makeText(context,"Log refreshed",Toast.LENGTH_SHORT).show();
    }

    private void showLog(TextView t)
    {
        ArrayList<String> time=new ArrayList<String>();
        ArrayList<String> msg=new ArrayList<String>();
        LogDBHelper helper=LogDBHelper.getLogHelperInstance(context);
        SQLiteDatabase db=helper.getUsableLogDB();
        String sql="select * from `logs`;";
        Cursor cur=db.rawQuery(sql, null);
        if(cur==null){
            return;
        }
        if(cur.moveToFirst())
        {
            do
            {
                String temp_tm=cur.getString(cur.getColumnIndex("time"));
                String temp_msg=cur.getString(cur.getColumnIndex("message"));
                time.add(temp_tm);
                msg.add(temp_msg);
            }
            while(cur.moveToNext());
            cur.close();
        }

        String message="";
        for(int i=0;i<time.size();i++)
        {
            message+=time.get(i)+" : "+msg.get(i)+"\n";
        }

        t.setText(message);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_fragment);
        TextView t= (TextView) findViewById(R.id.log_text);
        showLog(t);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logmenu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.clr_log : try{
                LogDBHelper clrlghelp=LogDBHelper.getLogHelperInstance(this);
                SQLiteDatabase log_cl=clrlghelp.getUsableLogDB();
                log_cl.execSQL("DELETE FROM `logs` WHERE 1;");
                //Toast.makeText(context,"Logs successfully deleted. Refresh to see updated log.",Toast.LENGTH_SHORT).show();
            }
            catch(Exception e)
            {
                e.printStackTrace();
                Log.e("Log","Deletion failed");
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
