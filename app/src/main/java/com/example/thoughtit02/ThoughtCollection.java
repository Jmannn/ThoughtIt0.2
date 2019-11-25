package com.example.thoughtit02;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class ThoughtCollection {
    List<Thought> currentSelectedThoughts;
    private DatabaseHelper mDatabaseHelper;
    Thought redo;

    public Thought getRedo() {
        return redo;
    }

    public int getRedoPosition() {
        return redoPosition;
    }

    int redoPosition;

    public ThoughtCollection(Context context) {
        this.mDatabaseHelper = new DatabaseHelper(context);
        this.currentSelectedThoughts = new ArrayList<>();
    }

    public boolean addThought(Thought thought) {
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

    public Thought getThought(int position) {
        return this.currentSelectedThoughts.get(position);
    }

    public int getDisplaySize() {
        return currentSelectedThoughts.size();
    }

    public boolean removeThought(int pos) {
        this.redoPosition = pos;
        this.redo = currentSelectedThoughts.get(pos);
        this.currentSelectedThoughts.remove(pos);
        boolean result = mDatabaseHelper.removeDatum(this.redo.getDateInMS());
        return result;
    }
    public boolean updateThought(int pos, String editedThought){
        Thought thought = this.currentSelectedThoughts.get(pos);
        boolean result = this.mDatabaseHelper.updateData(editedThought, thought.getDateInMS());
        if (result){
           thought.setThought(editedThought);
        }
        return result;
    }


}
