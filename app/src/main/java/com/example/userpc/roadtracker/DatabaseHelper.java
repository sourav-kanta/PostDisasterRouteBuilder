package com.example.userpc.roadtracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by USER PC on 5/21/2016.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME="location_service";
    private static final int VERSION=1;
    private static final String DB_CREATE=
            "CREATE TABLE `location` (`id` TEXT NOT NULL ," +
            " `longitude` REAL NOT NULL ," +
            " `latitude` REAL NOT NULL ," +
            " `TIME` DATETIME NOT NULL DEFAULT (strftime('%Y-%m-%d_%H:%M','now', 'localtime'))," +
            " UNIQUE (`id`, `longitude`, `latitude`, `TIME`));";
    
    private static SQLiteDatabase mDatabase=null;
    private static DatabaseHelper mDatabaseHelper=null;

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    public static synchronized DatabaseHelper getInstance(Context context)
    {
        if(mDatabaseHelper==null)
        	mDatabaseHelper=new DatabaseHelper(context);       //changed here
        return mDatabaseHelper;
    }
    
    
    
    public SQLiteDatabase getUsableDataBase()
    {
    	if(mDatabase==null)
    		mDatabase=getWritableDatabase();
    	return mDatabase;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
    	sqLiteDatabase.execSQL(DB_CREATE);
        Log.e("DB","Creating");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
