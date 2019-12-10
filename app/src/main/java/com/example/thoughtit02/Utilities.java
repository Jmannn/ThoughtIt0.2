package com.example.thoughtit02;

import java.util.Calendar;
import java.util.Date;
/* A class containing simple shared utilities.
 * @author Johnny Mann
 */
class Utilities {
    /* Used to compute yesterdays date and return it as a date object.
        @return the date object containing yesterdays date
     */
    static Date getYesterday(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        return calendar.getTime();
    }
    /* Computes tomorrows date
     * and returns the result in milliseconds.
     * @return MilliSeconds since 1970.
     */
    static long getTomorrow(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, +1);
        return calendar.getTimeInMillis();
    }
    /* Computes todays date
     * and returns the result in milliseconds.
     * @return MilliSeconds since 1970.
     */
    static long getToday(){
        Calendar calendar = Calendar.getInstance();
        return calendar.getTimeInMillis();
    }
    /* This method removes the trailing characters and then
     * checks if it is empty to determine whether it is blank.
     * @param str - The string to blank check.
     * @return True if blank, false otherwise.
     */
    static boolean checkBlank(String str){
        return str.trim().isEmpty();
    }
    /* This method removes the trailing characters and then
     * checks if it is empty to determine whether it is blank.
     * @param str[] - The strings to blank check.
     * @return True if blank, false otherwise.
     */
    static boolean checkBlank(String[] str){
        for (int i = 0; i < str.length; i++){
            if(Utilities.checkBlank(str[i])){
                return true;
            }
        }
        return false;
    }
    /* Used only to check whether the thoughts are the same. Uses a millisecond
     * threshold to because the date can be changed if there is a collision in the
     * database.
     * @param thought1 - Thought to compare to.
     * @param thought2 - Thought to compare against first.
     * @return Returns true if the fields of the objects are the same. */
    static public boolean isSameThought(Thought thought1, Thought thought2){
        final int msThreshold = 10;
        if(thought1.getType() != thought2.getType()) return false;
        else if (!thought1.getThoughtText().equals(thought2.getThoughtText())) return false;
        else if (Math.abs(thought1.getDateInMS() - thought2.getDateInMS()) > msThreshold) return false;
        else if (!thought1.getUri().equals(thought2.getUri())) return false;
        return true;
    }

}
