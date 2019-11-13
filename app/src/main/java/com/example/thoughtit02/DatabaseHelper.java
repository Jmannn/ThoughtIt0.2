package com.example.thoughtit02;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String TABLE_NAME = "thoughts_table";
    public static final String COL1 = "date_ms";
    public static final String COL2 = "thought";

    public DatabaseHelper(Context context){
        super(context,TABLE_NAME,null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE "+TABLE_NAME+ " ("+COL1+" INTEGER PRIMARY KEY " +
                "AUTOINCREMENT, " + COL2 + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " +TABLE_NAME);
        onCreate(db);
    }
    public boolean addData(Long date, String thought){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, date);
        contentValues.put(COL2, thought);
        Log.d(TAG, "addData: adding "+thought+" to table: "+TABLE_NAME);
        long result = db.insert(TABLE_NAME, null, contentValues);
        if(result == -1){
            return false;
        } else {
            return true;
        }
    }
    //TODO: Eventually this will ask for a range
    public Cursor getData(long lowerBound, long upperBound){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM "+TABLE_NAME+" WHERE "+COL1+" >= "+lowerBound+" " +
                "AND "+COL1+" <= "+upperBound;//+"' AND ORDER BY "+COL1;
        Log.d("DEBUG", query);
        Cursor data = db.rawQuery(query, null);
        return data;
    }
    public boolean removeDatum(long date){
        SQLiteDatabase db = this.getWritableDatabase();
        String clause = COL1+"=?";
        long result = db.delete(TABLE_NAME, clause, new String[]{Long.toString(date)});
        if(result == -1){
            return false;
        } else {
            return true;
        }
    }
    public Cursor searchData(String searchStr){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "Select * from "+TABLE_NAME+" where "+ COL2 +" like '%"+searchStr+"%'";
        Log.d("DEBUG", query);
        Cursor data = db.rawQuery(query, null);
        return data;

    }
}
