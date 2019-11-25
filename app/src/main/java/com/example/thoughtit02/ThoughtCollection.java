package com.example.thoughtit02;

import android.content.Context;
import android.database.Cursor;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

class ThoughtCollection {
    private List<Thought> currentSelectedThoughts;
    private DatabaseHelper mDatabaseHelper;
    private Thought redo;
    private int redoPosition;
    private Context context;

    Thought getRedo() {
        return this.redo;
    }

    int getRedoPosition() {
        return redoPosition;
    }
    boolean canRedo(){
        return redo != null;
    }
    /* This method deletes audio recordings and images from the system
     * once they are no longer needed. This should be called on pause and on stop.
     */
    void removeLastFile(){
        if(redo.getType() == Type.PICTURE || redo.getType() == Type.AUDIO){
            File file = new File(redo.getUri());
            boolean deleted = file.delete();
            if(!deleted){
                ((MainActivity)context).toastMessage("file: "+redo.getUri()
                        +" could not be deleted.");
            }
        }
    }
    void redo(){
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
    }

    ThoughtCollection(Context context) {
        this.context = context;
        this.mDatabaseHelper = new DatabaseHelper(context);
        this.currentSelectedThoughts = new ArrayList<>();
    }

    boolean addThought(Thought thought) {
        this.currentSelectedThoughts.add(thought);
        boolean insertData;
        String thoughtText = thought.getThoughtText();
        Long date = thought.getDateInMS();
        String uri = thought.getUri();

        if (thought.getType() == Type.PICTURE) {
            insertData = mDatabaseHelper.addData(date, thoughtText, "Picture", uri);
        } else if (thought.getType() == Type.AUDIO) {
            insertData = mDatabaseHelper.addData(date, thoughtText, "Audio", uri);
        } else {
            insertData = mDatabaseHelper.addData(date, thoughtText, "Text", uri);
        }
        return insertData;
    }

    Thought getThought(int position) {
        return this.currentSelectedThoughts.get(position);
    }

    int getDisplaySize() {
        return currentSelectedThoughts.size();
    }

    boolean removeThought(int pos) {
        removeLastFile();
        this.redoPosition = pos;
        this.redo = currentSelectedThoughts.get(pos);
        this.currentSelectedThoughts.remove(pos);
        return mDatabaseHelper.removeDatum(this.redo.getDateInMS());
    }
    boolean updateThought(int pos, String editedThought){
        Thought thought = this.currentSelectedThoughts.get(pos);
        boolean result = this.mDatabaseHelper.updateData(editedThought, thought.getDateInMS());
        if (result){
           thought.setThought(editedThought);
        }
        return result;
    }
    private void displayData(Cursor cursor){
        this.currentSelectedThoughts.clear();
        while(cursor.moveToNext()){
            this.currentSelectedThoughts.add(new Thought(cursor.getLong(0),
                    cursor.getString(1),cursor.getString(2), cursor.getString(3)));
        }
    }
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
    void searchAndDisplay(String searchStr){
        displayData(mDatabaseHelper.searchData(searchStr));
    }
    void clearThoughts(){
        mDatabaseHelper.clearDatabase();
        this.currentSelectedThoughts.clear();
    }


}
