package com.example.thoughtit02;


import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;


import java.util.Date;


//Todo: test add
//Todo: test search
//Todo: test all possible inputs and outputs
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ThoughtCollectionTest {
    private static ThoughtCollection thoughts;


    @BeforeClass
    public static void setUp(){
        thoughts = new ThoughtCollection(InstrumentationRegistry.getInstrumentation().getTargetContext());
    }
    @Before
    public void databaseClear(){

    }
    @Test
    public void addThoughts() {
        Date today = new Date();
        Thought testThought = new Thought(new Date(), "Just a mock test.", Type.TEXT, "");
        thoughts.addThought(testThought);
        thoughts.clearDisplay();
        thoughts.prepareDataSet(Utilities.getYesterday(), today);
        assert(isSameThought(testThought, thoughts.getThought(0)));

    }

    @Test
    public void canRedo() {
    }
    /* Used only to check whether the thoughts are the same.
     * @param thought1 - Thought to compare to.
     * @param thought2 - Thought to compare against first.
     * @return Returns true if the fields of the objects are the same. */
    public boolean isSameThought(Thought thought1, Thought thought2){
        if(thought1.getType() != thought2.getType()) return false;
        else if (!thought1.getThoughtText().equals(thought2.getThoughtText())) return false;
        else if (thought1.getDateInMS() != thought2.getDateInMS()) return false;
        else if (thought1.getUri() != thought2.getUri()) return false;
        return true;
    }

}