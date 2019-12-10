package com.example.thoughtit02;

import android.os.SystemClock;

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

import static com.example.thoughtit02.Utilities.getYesterday;
import static com.example.thoughtit02.Utilities.isSameThought;
import static junit.framework.TestCase.assertTrue;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ThoughtCollectionTest {
    private static ThoughtCollection thoughtsCollection;


    @BeforeClass
    public static void setUp(){
        thoughtsCollection = new ThoughtCollection(InstrumentationRegistry.getInstrumentation().
                getTargetContext(), "test_table");
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
        thoughtsCollection.prepareDataSet(getYesterday(), today);
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
    public void testThatAllThoughtsAdded(){
        final int numberToAdd = 100, lowestTime = 0;

        fillModelConsecutive(thoughtsCollection, numberToAdd);
        thoughtsCollection.clearDisplay();
        thoughtsCollection.prepareDataSet(new Date(lowestTime), new Date());
        assertTrue(thoughtsCollection.getDisplaySize() == numberToAdd);
    }
    /* This test checks whether an empty search string to show every of the entries
     * in the correct order.
     */
    @Test
    public void searchThoughtsEmptySearchString(){
        List<Thought> thoughtsToCheck = new ArrayList<>();
        thoughtsToCheck.add(new Thought(new Date(), "ExampleTest", Type.TEXT, "file/sart.smg"));
        thoughtsToCheck.add(new Thought(new Date(), "ExampleTest1", Type.AUDIO, "file/smar.smeg"));
        thoughtsToCheck.add(new Thought(new Date(), "ExampleTest2", Type.PICTURE, "file/smat.smga"));
        for(Thought thought : thoughtsToCheck){
            thoughtsCollection.addThought(thought);
        }
        thoughtsCollection.clearDisplay();
        thoughtsCollection.searchAndDisplay("");
        for (int i = 0 ; i < thoughtsCollection.getDisplaySize(); i++){
            assertTrue(isSameThought(thoughtsCollection.getThought(i),thoughtsToCheck.get(i)));
        }
    }
    /* Search model for a substring and display all results that contain this substring. */
    @Test
    public void searchSubstring(){
        List<Thought> thoughtsToCheck = new ArrayList<>();
        List<Thought> thoughtsToAdd = new ArrayList<>();

        thoughtsToCheck.add(new Thought(new Date(), "prefixExampleTest2", Type.PICTURE, "file/smat.smga"));
        thoughtsToCheck.add(new Thought(new Date(), "ExampleTest1", Type.AUDIO, "file/smar.smeg"));
        thoughtsToCheck.add(new Thought(new Date(), "Example", Type.TEXT, "file/sart.smg"));

        thoughtsToAdd.add(new Thought(new Date(), "prefixExleTest2", Type.PICTURE, "file/smat.smga"));
        thoughtsToAdd.add(thoughtsToCheck.get(0));
        thoughtsToAdd.add(new Thought(new Date(), "pre", Type.AUDIO, "file/sat.smga"));
        thoughtsToAdd.add(thoughtsToCheck.get(1));
        thoughtsToAdd.add(new Thought(new Date(), "prefixExeTest2", Type.TEXT, "file/smat.smga"));
        thoughtsToAdd.add(thoughtsToCheck.get(2));

        for(Thought thought : thoughtsToAdd){
            thoughtsCollection.addThought(thought);
        }
        thoughtsCollection.clearDisplay();
        thoughtsCollection.searchAndDisplay("Example");

        for (int i = 0 ; i < thoughtsCollection.getDisplaySize(); i++){
            assertTrue(isSameThought(thoughtsCollection.getThought(i),thoughtsToCheck.get(i)));
        }
    }
    /* This test should add a thought, remove it, then try and redo it. */
    @Test
    public void canRedoPass() {
        Thought thoughtToRedo;
        int size;
        thoughtsCollection.addThought(new Thought(new Date(), "prefixExampleTest2", Type.PICTURE, "file/smat.smga"));
        thoughtsCollection.addThought(new Thought(new Date(), "ExampleTest1", Type.AUDIO, "file/smar.smeg"));
        thoughtsCollection.addThought(new Thought(new Date(), "Example", Type.TEXT, "file/sart.smg"));
        size = thoughtsCollection.getDisplaySize();
        thoughtToRedo = thoughtsCollection.getThought(1);
        thoughtsCollection.removeThought(1);
        assertTrue(thoughtsCollection.getDisplaySize() == size -1);
        thoughtsCollection.redo();
        assertTrue(isSameThought(thoughtsCollection.getThought(1), thoughtToRedo));
    }
    /* This test should try to redo a thought but there will be nothing to redo. Note
     * that a prior bug meant that even after clear, it still had a thought it could redo.
     * This test is designed to catch that.  */
    @Test
    public void canRedoFail(){
        int size;
        thoughtsCollection.addThought(new Thought(new Date(), "prefixExampleTest2", Type.PICTURE, "file/smat.smga"));
        thoughtsCollection.addThought(new Thought(new Date(), "ExampleTest1", Type.AUDIO, "file/smar.smeg"));
        thoughtsCollection.addThought(new Thought(new Date(), "Example", Type.TEXT, "file/sart.smg"));
        size = thoughtsCollection.getDisplaySize();
        thoughtsCollection.redo();
        assertTrue(thoughtsCollection.getDisplaySize() == size);
    }
    /* Checks that the max date being returned is indeed the last entered. */
    @Test
    public void testMaxDate(){
        Thought max;
        thoughtsCollection.addThought(new Thought(new Date(), "prefixExampleTest2", Type.PICTURE, "file/smat.smga"));
        thoughtsCollection.addThought(new Thought(new Date(), "ExampleTest1", Type.AUDIO, "file/smar.smeg"));
        SystemClock.sleep(10);
        max = new Thought(new Date(), "Example", Type.TEXT, "file/sart.smg");
        thoughtsCollection.addThought(max);
        assertTrue(thoughtsCollection.largestDate() == max.getDateInMS());
    }
    /* Checks that the min date being returned is indeed the first entered. */
    @Test
    public void testMinDate(){
        SystemClock.sleep(10);
        Thought min = new Thought(new Date(), "prefixExampleTest2", Type.PICTURE, "file/smat.smga");
        thoughtsCollection.addThought(min);
        thoughtsCollection.addThought(new Thought(new Date(), "ExampleTest1", Type.AUDIO, "file/smar.smeg"));
        thoughtsCollection.addThought(new Thought(new Date(), "Example", Type.TEXT, "file/sart.smg"));
        assertTrue(thoughtsCollection.smallestDate() == min.getDateInMS());
    }
    /* This test should check if a thought is updated correctly. */
    @Test
    public void canUpdate(){
        String updatedString = "UpdateExample";
        int posToCheck = 2;
        thoughtsCollection.addThought(new Thought(new Date(), "prefixExampleTest2", Type.PICTURE, "file/smat.smga"));
        thoughtsCollection.addThought(new Thought(new Date(), "ExampleTest1", Type.AUDIO, "file/smar.smeg"));
        thoughtsCollection.addThought(new Thought(new Date(), "Example", Type.TEXT, "file/sart.smg"));
        thoughtsCollection.updateThought(posToCheck, updatedString);
        thoughtsCollection.clearDisplay();
        thoughtsCollection.searchAndDisplay("");
        assertTrue(thoughtsCollection.getThought(posToCheck).getThoughtText().equals(updatedString));
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
            if(typeChosen == Type.TEXT) {
                thoughtCollection.addThought(new Thought(new Date(), "Basic thought", typeChosen, ""));
            } else {
                thoughtCollection.addThought(new Thought(new Date(), "Basic thought", typeChosen, "fake/fakelib/meda.bmg"));
            }
        }
    }
    /* Fills the model out of order. The model should return in order regardless.
     * @param thoughtCollection - A reference to the thoughtCollection model.
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