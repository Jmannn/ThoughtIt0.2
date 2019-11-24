package com.example.thoughtit02;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String TABLE_NAME = "thought_log_table";
    public static final String COL1 = "date_ms";
    public static final String COL2 = "thought";
    public static final String COL3 = "type";
    public static final String COL4 = "url";

    public DatabaseHelper(Context context){
        super(context,TABLE_NAME,null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE "+TABLE_NAME+ " ("+COL1+" INTEGER PRIMARY KEY " +
                "AUTOINCREMENT, " + COL2 + " TEXT,"+ COL3 + " TEXT,"+ COL4 + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " +TABLE_NAME);
        onCreate(db);
    }
    public void clearDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        String clearDBQuery = "DELETE FROM "+TABLE_NAME;
        db.delete(TABLE_NAME, null, null);
        db.execSQL(clearDBQuery);
    }
    public boolean addData(Long date, String thought, String type, String url){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, date);
        contentValues.put(COL2, thought);
        contentValues.put(COL3, type);
        contentValues.put(COL4, url);
        Log.d(TAG, "addData: adding "+thought+" to table: "+TABLE_NAME);
        long result = db.insert(TABLE_NAME, null, contentValues);
        if(result == -1){
            return false;
        } else {
            return true;
        }
    }

    public boolean updateData(String newThought, long date){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL2, newThought);
        long result =  db.update(TABLE_NAME, cv, COL1 + "= ?", new String[] {Long.toString(date)});
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
                "AND "+COL1+" <= "+upperBound + " ORDER BY " +COL1;//+"' AND ORDER BY "+COL1;
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
        String query = "Select * from "+TABLE_NAME+" where "+ COL2 +" like '%"+searchStr+"%'"+ " ORDER BY " +COL1;
        Log.d("DEBUG", query);
        Cursor data = db.rawQuery(query, null);
        return data;

    }
}
