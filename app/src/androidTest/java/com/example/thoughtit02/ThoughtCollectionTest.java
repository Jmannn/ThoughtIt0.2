package com.example.thoughtit02;


import android.os.SystemClock;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static junit.framework.TestCase.assertTrue;


//Todo: test add
//Todo: test all possible inputs and outputs
//Todo: test search, incl empty string, numbers, normal, do a couple that test how many it find
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ThoughtCollectionTest {
    private static ThoughtCollection thoughtsCollection;


    @BeforeClass
    public static void setUp(){
        thoughtsCollection = new ThoughtCollection(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_table");
        //generate the thoughts here, then test them coming out
    }
    @Before
    public void databaseClear(){
        thoughtsCollection.clearThoughts();
    }
    @Test
    public void addThoughts() {
        Date today = new Date();
        Thought testThought = new Thought(new Date(), "Just a mock test.", Type.TEXT, "");
        thoughtsCollection.addThought(testThought);
        thoughtsCollection.clearDisplay();
        thoughtsCollection.prepareDataSet(Utilities.getYesterday(), today);
        Log.e("DEBG", " : : "+testThought.getThoughtText()+" "+testThought.getDateInMS());
        Log.e("DEBG", " :+: "+thoughtsCollection.getThought(0).getThoughtText()+" "+thoughtsCollection.getThought(0).getDateInMS());
        System.out.println("sdfsdfsdf");
        System.err.println("sdffsdfsdfs");
        assertTrue(isSameThought(testThought, thoughtsCollection.getThought(0)));

    }
    /* The correct order should be the order of the dates in ascending order so
     * each following date retrieved should be greater than the last.
     * Thoughts added in order.
     */
    @Test
    public void testCorrectOrder(){
        Thought lastThought;
        fillModelConsecutive(thoughtsCollection, 100);
        thoughtsCollection.clearDisplay();
        thoughtsCollection.prepareDataSet(new Date(0), new Date());
        lastThought = thoughtsCollection.getThought(0);
        for(int i = 1; i < thoughtsCollection.getDisplaySize(); ++i){
            assertTrue(thoughtsCollection.getThought(i).getDateInMS() > lastThought.getDateInMS());
            lastThought = thoughtsCollection.getThought(i);
        }
    }
    /* The correct order should be the order of the dates in ascending order so
     * each following date retrieved should be greater than the last.
     * Thoughts added out of order.
     */
    @Test
    public void testCorrectOrderAddOutOfOrder(){
        Thought lastThought;
        fillModelNotConsecutive(thoughtsCollection, 100);
        thoughtsCollection.clearDisplay();
        thoughtsCollection.prepareDataSet(new Date(0), new Date());
        lastThought = thoughtsCollection.getThought(0);
        for(int i = 1; i < thoughtsCollection.getDisplaySize(); ++i){
            assertTrue(thoughtsCollection.getThought(i).getDateInMS() > lastThought.getDateInMS());
            lastThought = thoughtsCollection.getThought(i);
        }
    }
    /* This test makes sure that each item was added. Previous error was that if
     * two dates added at same time then one would not added since its primary key would
     * clash.
     */
    @Test
    public void testAllThoughtsAdded(){
        Thought lastThought;
        int numberToAdd = 100;
        fillModelConsecutive(thoughtsCollection, numberToAdd);
        thoughtsCollection.clearDisplay();
        thoughtsCollection.prepareDataSet(new Date(0), new Date());
        assertTrue(thoughtsCollection.getDisplaySize() == numberToAdd);
    }

    @Test
    public void canRedoPass() {
    }
    @Test
    public void canRedoFail(){

    }

    /* Used only to check whether the thoughts are the same. Uses a millisecond
     * threshold to because the date can be changed if there is a collision in the
     * database.
     * @param thought1 - Thought to compare to.
     * @param thought2 - Thought to compare against first.
     * @return Returns true if the fields of the objects are the same. */
    public boolean isSameThought(Thought thought1, Thought thought2){
        final int msThreshold = 10;
        if(thought1.getType() != thought2.getType()) return false;
        else if (!thought1.getThoughtText().equals(thought2.getThoughtText())) return false;
        else if (Math.abs(thought1.getDateInMS() - thought2.getDateInMS()) > msThreshold) return false;
        else if (!thought1.getUri().equals(thought2.getUri())) return false;
        return true;
    }
    /* This method fills the model with a bunch of consecutively occurring thoughts.
     * @param thoughtCollection - A reference to the thoughtcollection model.
     * @param numberToGenerate - This is the number of thoughts to generate before the test.
     */
    public void fillModelConsecutive(ThoughtCollection thoughtCollection, int numberToGenerate){
        Random random = new Random();
        Type[] types = {Type.TEXT, Type.PICTURE, Type.AUDIO};
        Type typeChosen;
        for (int i = 0; i < numberToGenerate; ++i){
            typeChosen = types[random.nextInt(types.length)];
            //SystemClock.sleep(1);//Needs to sleep otherwise date will cause clash
            if(typeChosen == Type.TEXT) {
                thoughtCollection.addThought(new Thought(new Date(), "Basic thought", typeChosen, ""));
            } else {
                thoughtCollection.addThought(new Thought(new Date(), "Basic thought", typeChosen, "fake/fakelib/meda.bmg"));
            }
        }
    }
    /* Fills the model out of order. The model should return in order regardless.
     * @param thoughtCollection - A reference to the thoughtcollection model.
     * @param numberToGenerate - This is the number of thoughts to generate before the test.
     */
    public void fillModelNotConsecutive(ThoughtCollection thoughtCollection, int numberToGenerate){
        Random random = new Random();
        List<Thought> thoughts = new ArrayList<>();
        Type[] types = {Type.TEXT, Type.PICTURE, Type.AUDIO};
        Type typeChosen;
        int thoughtChoice;
        for (int i = 0; i < numberToGenerate; ++i){
            typeChosen = types[random.nextInt(types.length)];
            //SystemClock.sleep(1);
            if(typeChosen == Type.TEXT) {
                thoughts.add(new Thought(new Date(), "Basic thought", typeChosen, ""));
            } else {
                thoughts.add(new Thought(new Date(), "Basic thought", typeChosen, "fake/fakelib/meda.bmg"));
            }
        }
        while(thoughts.size() >0){
            thoughtChoice = random.nextInt(thoughts.size());
            thoughtCollection.addThought(thoughts.get(thoughtChoice));
            thoughts.remove(thoughtChoice);
        }
    }

}