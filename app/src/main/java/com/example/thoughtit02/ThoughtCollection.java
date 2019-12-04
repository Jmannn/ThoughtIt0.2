package com.example.thoughtit02;

import android.content.Context;
import android.database.Cursor;
import android.os.SystemClock;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
/* This class is contains a collection of thoughts and
 * the operations required to fetch more information to
 * display, add, and remove.
 * @author Johnny Mann
 */
class ThoughtCollection {
    /* The current thoughts that are meant to be displayed in RecyclerView. */
    private List<Thought> currentSelectedThoughts;
    /* The database helper for adding thoughts as rows to the database. */
    private DatabaseHelper mDatabaseHelper;
    /* Contains the last redo thought. */
    private Thought redo;
    /* Contains the original position of the last redo. */
    private int redoPosition;
    /* The main activity context. */
    private Context context;
    /* Gets the thought to re add.
     * @return The thought to re add.
     */
    Thought getRedo() {
        return this.redo;
    }
    boolean canRedo(){
        return this.redo != null;
    }
    /* The position to restore the thought to. */
    int getRedoPosition() {
        return redoPosition;
    }
    /* This method deletes audio recordings and images from the system
     * once they are no longer needed. This should be called on pause and on stop.
     */
    void removeLastFile(){
        if (this.redo == null){
            return;
        }
        else if(redo.getType() == Type.PICTURE || redo.getType() == Type.AUDIO){
            removeFile(redo.getUri());
        }
    }
    /* This method removes a file from file storage.
     * @param filepath - This is the filepath to the file to remove.
     * @return True if file is successfully removed.
     */
    private void removeFile(String filepath){
        File file = new File(redo.getUri());
        if(!file.delete()){
            ((MainActivity)context).toastMessage("file: "+filepath
                    +" could not be deleted.");
        }
    }
    /* Performs the operation to re add the thought back
     * into the database, and the thoughts to display.
     */
    boolean redo(){
        if (!canRedo()) return false;
        this.currentSelectedThoughts.add(redoPosition, redo);
        String dataType = "";
        switch (redo.getType()){
            case TEXT:
                dataType = "Text";
                break;
            case AUDIO:
                dataType = "Audio";
                break;
            case PICTURE:
                dataType = "Picture";
                break;
        }
        this.mDatabaseHelper.addData(redo.getDateInMS(), redo.getThoughtText(), dataType, redo.getUri());
        this.redo = null;
        return true;
    }
    /* Constructor creates a databasehelper object using main activity context.
     * @param context - The main activity context.
     * @param dbTableName - Allows you to specify a database tablename. Mostly for testing.
     */
    ThoughtCollection(Context context, String dbTableName) {
        this.context = context;
        this.mDatabaseHelper = new DatabaseHelper(context, dbTableName);
        this.currentSelectedThoughts = new ArrayList<>();
    }
    /* Constructor creates a databasehelper object using main activity context.
     * @param context - The main activity context.
     * @param dbTabName - Allows you to specify a database tablename. Mostly for testing.
     */
    ThoughtCollection(Context context) {
        this.context = context;
        this.mDatabaseHelper = new DatabaseHelper(context);
        this.currentSelectedThoughts = new ArrayList<>();
    }

    /* Adds a thought to both the display arraylist currentSelectedThoughts and
     * the database. This does need wait a millisecond before adding to insure
     * a unique time value.
     * @param thought - The thought object to add.
     * @return whether it was successfully added.
     */
    boolean addThought(Thought thought) {
        this.currentSelectedThoughts.add(thought);
        boolean insertData = false;
        String thoughtText = thought.getThoughtText();
        Long date = thought.getDateInMS();
        String uri = thought.getUri();

        final int totalAttempts = 2;
        int attemptsCount = 0;
        while(!insertData && attemptsCount < totalAttempts) {
            if (thought.getType() == Type.PICTURE) {
                insertData = mDatabaseHelper.addData(date, thoughtText, "Picture", uri);
            } else if (thought.getType() == Type.AUDIO) {
                insertData = mDatabaseHelper.addData(date, thoughtText, "Audio", uri);
            } else {
                insertData = mDatabaseHelper.addData(date, thoughtText, "Text", uri);
            }
            if(!insertData){
                ++attemptsCount;
                thought.resetDate();
                date = thought.getDateInMS();
            }
        }
        return insertData;
    }
    /* This method returns the newest thought in the display.
     * @return The news thought added. */
    private Thought getLatest(){
        return this.currentSelectedThoughts.get(this.getDisplaySize()-1);
    }
    /* Returns a thought at a given position in the currentSelectedThoughts arraylist.
     * @param position - The position to retrieve the thought from.
     * @return the thought at the given position.
     */
    Thought getThought(int position) {
        return this.currentSelectedThoughts.get(position);
    }
    /* Returns the size of the thoughts to display.
     * @return the number of thoughts corresponding to the size
     *  of the display.
     */
    int getDisplaySize() {
        return currentSelectedThoughts.size();
    }
    /* Removes a thought from both the display arraylist currentSelectedThoughts and
     * the database. Permanently removes the file stored in redo, since it will now
     * be adding another to redo.
     * @param pos - The position of the thought in the currentSelectedThoughts
     *  display list.
     * @return whether it was successfully removed.
     */
    boolean removeThought(int pos) {
        removeLastFile(); //removes last file to make way for another file to be added as redo.
        this.redoPosition = pos;
        this.redo = currentSelectedThoughts.get(pos);
        this.currentSelectedThoughts.remove(pos);
        return mDatabaseHelper.removeDatum(this.redo.getDateInMS());
    }
    /* Updates the text of a given thought.
     * @param pos - The position of the thought in the currentSelectedThoughts list.
     * @return Whether this operation was successful.
     */
    boolean updateThought(int pos, String editedThought){
        Thought thought = this.currentSelectedThoughts.get(pos);
        boolean result = this.mDatabaseHelper.updateData(editedThought, thought.getDateInMS());
        if (result){
           thought.setThought(editedThought);
        }
        return result;
    }
    /* Takes a cursor, extracts the table row information into Thought
     * objects which are added to the current displayed thoughts.
     * @param cursor - The cursor pointing to the database query results.
     */
    private void displayData(Cursor cursor){
        this.currentSelectedThoughts.clear();
        while(cursor.moveToNext()){
            this.currentSelectedThoughts.add(new Thought(cursor.getLong(0),
                    cursor.getString(1),cursor.getString(2), cursor.getString(3)));
        }
    }
    /* This method will simply clear everything from the
     * thought list but will not delete from the Database.
     */
    public void clearDisplay(){
        this.currentSelectedThoughts.clear();
    }
    /* Prepares and executes a database query which returns
     * results within the date range specified.
     * @param lower - The minimum date allowed to be displayed.
     * @param upperBound - The maximum date allowed to be displayed.
     */
    void prepareDataSet(Date lower, Date upperBound){
        long lowerBound;
        Calendar cal = Calendar.getInstance();
        cal.setTime(lower);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        lowerBound = cal.getTimeInMillis();

        cal.setTime(upperBound);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        displayData(mDatabaseHelper.getData(lowerBound, cal.getTimeInMillis()));
    }
    /* Instructs the database helper to execute a query to
     * return all results containing searchStr substring.
     * @param searchStr - The substring to check the database table for.
     */
    void searchAndDisplay(String searchStr){
        displayData(mDatabaseHelper.searchData(searchStr));
    }
    /* Clears the database, and clears the current selected thoughts. */
    void clearThoughts(){
        mDatabaseHelper.clearDatabase();
        this.currentSelectedThoughts.clear();
        //TODO: This needs to clear from the actual filesystem as well.
    }


}
