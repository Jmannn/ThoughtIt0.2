package com.example.thoughtit02;

import java.util.Calendar;
import java.util.Date;

class Utilities {
    /* Used to compute yesterdays date and return it as a date object.
        @return the date object containing yesterdays date
     */
    static Date getYesterday(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        return calendar.getTime();
    }

}
