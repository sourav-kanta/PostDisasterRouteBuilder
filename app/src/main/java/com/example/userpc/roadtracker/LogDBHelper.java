package com.example.userpc.roadtracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by USER PC on 6/13/2016.
 */
public class LogDBHelper extends SQLiteOpenHelper{

    private static final String DBNAME="Log_DB";
    private static final int VERSION=1;
    private static final String DBCREATE="CREATE TABLE `logs` (`time` TEXT NOT NULL , `message` TEXT NOT NULL);";

    private static LogDBHelper logDBHelper=null;
    private static SQLiteDatabase logDB=null;

    public static synchronized LogDBHelper getLogHelperInstance(Context context)
    {
        if(logDBHelper==null)
            logDBHelper=new LogDBHelper(context);
        return logDBHelper;
    }

    public SQLiteDatabase getUsableLogDB()
    {
        if(logDB==null)
            logDB=this.getWritableDatabase();
        return logDB;
    }

    private LogDBHelper(Context context) {
        super(context,DBNAME,null,VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DBCREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //future releases
    }
}
