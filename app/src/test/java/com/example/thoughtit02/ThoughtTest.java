package com.example.thoughtit02;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

public class ThoughtTest {

    @Before
    public void setUp() throws Exception {

    }
    @After
    public void tearDown() throws Exception {
        System.out.println("Test Completed");
    }
    /* -1 is not a time value, ms since 1970. */
    @Test (expected = IllegalArgumentException.class)
    public void thoughtTimeNegative() {
        Thought thoughtTimeNegative = new Thought(-1L, "Not a real time",
                Type.AUDIO, "android/3gp");
    }
    /* You should not able to have a thought that is in the future. */
    @Test (expected = IllegalArgumentException.class)
    public void thoughtTimeGreaterThanToday() {
        Thought thoughtTimeGreaterThanToday = new Thought(Utilities.getTomorrow(), "Not a real time",
                Type.AUDIO, "android/3gp");
    }
    @Test (expected = IllegalArgumentException.class)
    public void thoughtStringDoesNotMap() {
        Thought thoughtTimeGreaterThanToday = new Thought(100000000L, "Not a real time",
                "Audi", "android/3gp");
    }
    /* This test should fail because it is illegal to have a non text type with no uri. */
    @Test (expected = IllegalArgumentException.class)
    public void nonTextTypeNoUri() {
        Thought thoughtTypeNonTextNoURI = new Thought(Utilities.getYesterday().getTime(),
                "Uri is empty",
                "Audio",
                "");
        Thought thoughtTypeNonTextURIWhiteSpace = new Thought(Utilities.getYesterday().getTime(),
                "Uri is whitespace",
                "Picture",
                "     ");
    }
    /* Only the URI should be blank in this case since, Text requires no URI. */
    @Test (expected = IllegalArgumentException.class)
    public void whiteSpace() {
        Thought noWhiteSpace = new Thought(100000000L, "",
                "", "");
        noWhiteSpace = new Thought(100000000L, "",
                Type.TEXT, "");
        noWhiteSpace = new Thought(Utilities.getYesterday(), "",
                Type.TEXT, "");
    }
    /* Test should be able to except todays date. */
    @Test
    public void todayDate(){
        Thought testTodaysDate = new Thought(new Date(), "Example thought.", Type.AUDIO, "android/audio.3gp");
    }
    /* The thought type should map and this test should pass. */
    @Test
    public void thoughtStringTypeDoesMap(){
        Thought thoughtStringTypeDoesMap = new Thought(Utilities.getYesterday().getTime(), "Example thought.", "Audio", "android/audio.3gp");
        assert (thoughtStringTypeDoesMap.getType() == Type.AUDIO);
        thoughtStringTypeDoesMap = new Thought(Utilities.getYesterday().getTime(), "Example thought.", "Text", "");
        assert (thoughtStringTypeDoesMap.getType() == Type.TEXT);
        thoughtStringTypeDoesMap = new Thought(Utilities.getYesterday().getTime(), "Example thought.", "Picture", "android/picture.jpg");
        assert (thoughtStringTypeDoesMap.getType() == Type.PICTURE);
    }
    /* This is the lowest possible time in milliseconds. */
    @Test
    public void validTimeSet(){
        Thought thoughtTimeZero = new Thought(0L,"Example thought.", "Audio", "android/audio.3gp");
    }


}