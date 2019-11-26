package com.example.thoughtit02;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/* This class is used to format thoughts into db queries which
 * it then adds to the Android SQLite database.
 * @author Johnny Mann
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    /* Class tag for log. */
    private static final String TAG = "DatabaseHelper";
    /* The table name in database. */
    private static final String TABLE_NAME = "thought_log_table";
    /* Names of the columns of table. */
    private static final String COL1 = "date_ms";
    private static final String COL2 = "thought";
    private static final String COL3 = "type";
    private static final String COL4 = "url";
    /* Activity context. */
    private Context context;

    DatabaseHelper(Context context){
        super(context,TABLE_NAME,null,1);
        this.context = context;
    }

    /* Prepares table creation query then creates table in the
     * database.
     * @param database
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = context.getString(R.string.CreateTableSQL)+TABLE_NAME+ " ("+COL1+context.getString(R.string.IntPrimaryKeySQL) +
                context.getString(R.string.AutoIncrementSQL) + COL2 + " TEXT,"+ COL3 + " TEXT,"+ COL4 + " TEXT)";
        db.execSQL(createTable);
    }
    /* Upgrades the table.
     * @param database
     * @param old table version
     * @param new table version
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(context.getString(R.string.ForgetTableSQL) +TABLE_NAME);
        onCreate(db);
    }
    /* Used to clear the table if the user wants to erase their data.
     */
    void clearDatabase() {
        SQLiteDatabase db;
        db = this.getWritableDatabase();
        String clearDBQuery = context.getString(R.string.SQLDelete)+TABLE_NAME;
        db.delete(TABLE_NAME, null, null);
        db.execSQL(clearDBQuery);
    }
    /* Adds a row of data to a table.
     * @param the date in ms
     * @param thought text
     * @param the type of file as a string
     * @param the uri as a string
     * @return successfully added
     */
    boolean addData(Long date, String thought, String type, String url){
        SQLiteDatabase db;
        ContentValues contentValues;
        db = this.getWritableDatabase();
        contentValues = new ContentValues();
        contentValues.put(COL1, date);
        contentValues.put(COL2, thought);
        contentValues.put(COL3, type);
        contentValues.put(COL4, url);
        Log.d(TAG, "addData: adding "+thought+" to table: "+TABLE_NAME);
        long result = db.insert(TABLE_NAME, null, contentValues);
        return !(result == -1);
    }
    /* Uses date in milliseconds to change to value of
     * column 2 which is the thought text.
     * @param updated thought text
     * @param date of thought to update
     * @return true if success
     */
    boolean updateData(String newThought, long date){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL2, newThought);
        long result =  db.update(TABLE_NAME, cv, COL1 + "= ?", new String[] {Long.toString(date)});
        return !(result == -1);

    }
    /* Returns data in a given range from the database.
     * @param the lower bound of date range
     * @param the upper bound of date range
     * @return the cursor pointing to the elements selected
     */
    Cursor getData(long lowerBound, long upperBound){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM "+TABLE_NAME+" WHERE "+COL1+" >= "+lowerBound+" " +
                "AND "+COL1+" <= "+upperBound + " ORDER BY " +COL1;//+"' AND ORDER BY "+COL1;
        Log.d("DEBUG", query);
        return db.rawQuery(query, null);
    }
    /* Removes a thought using date value as the delete key.
     * @param date in milliseconds
     * @return true if successful
     */
    boolean removeDatum(long date){
        SQLiteDatabase db = this.getWritableDatabase();
        String clause = COL1+"=?";
        long result = db.delete(TABLE_NAME, clause, new String[]{Long.toString(date)});
        return !(result == -1);
    }
    /* Returns every element in the table which contains the
     * given substring.
     * @param substring
     * @return cursor pointing to the elements selected
     */
    Cursor searchData(String searchStr){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "Select * from "+TABLE_NAME+" where "+ COL2 +" like '%"+searchStr+"%'"+ " ORDER BY " +COL1;
        Log.d("DEBUG", query);
        return db.rawQuery(query, null);

    }
}
