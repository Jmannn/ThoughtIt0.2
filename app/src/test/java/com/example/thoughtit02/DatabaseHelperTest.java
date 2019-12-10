package com.example.thoughtit02;

import android.database.Cursor;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
import static com.example.thoughtit02.Utilities.isSameThought;

public class DatabaseHelperTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void clearDatabase() {
    }

    @Test
    public void addData() {
    }

    @Test
    public void selectMin() {
    }

    @Test
    public void selectMax() {
    }

    @Test
    public void updateData() {
    }

    @Test
    public void getData() {
    }

    @Test
    public void removeDatum() {
    }

    @Test
    public void searchData() {
    }
    /* This helper method should take a cursor and an list of thoughts as input and confirm that
     * the cursor contains only the thoughts in that list. The ordering of the list should match
     * the order of the cursor.
     * @param cursor - Points to the list of thoughts being returned by the database mock
     * @param thoughts - A list of thoughts to confirm exist in the database.
     * @return Should return true if the rows of the database returned match the thoughts as input.
     */
    public boolean confirmCorrectThoughtsReturned(Cursor cursor, List<Thought> thoughtsToReturn){
        int count = 0;
        Thought thought;
        while(cursor.moveToNext()){
            thought = new Thought(cursor.getLong(0),
                    cursor.getString(1),cursor.getString(2), cursor.getString(3));
            if(isSameThought(thought, thoughtsToReturn.get(++count))) return false;
        }
        return true;

    }
}