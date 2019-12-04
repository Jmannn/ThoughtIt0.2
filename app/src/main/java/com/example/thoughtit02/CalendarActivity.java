package com.example.thoughtit02;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.CalendarView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Date;

/* This class activity allows the user to select a date from
 * the calender.
 * @author Johnny Mann
 */
public class CalendarActivity extends AppCompatActivity {
    /* For log tags. */
    private final String TAG = "CalendarActivity";
    final String LOWER_BOUND_ID = "LOWER_DATE";
    final String UPPER_BOUND_ID = "UPPER_DATE";

    /* Called when class created. Waits for the user to click on a date. Then
     * starts an intent back to main activity with added date.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_layout);
        CalendarView mCalendarView = findViewById(R.id.calendarView);
        mCalendarView.setMinDate(getIntent().getLongExtra(LOWER_BOUND_ID, 0));
        mCalendarView.setMaxDate(getIntent().getLongExtra(UPPER_BOUND_ID, new Date().getTime()));
        mCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month,dayOfMonth);

                Intent intent = new Intent(CalendarActivity.this, MainActivity.class);
                intent.putExtra(getString(R.string.DateIntentID), calendar.getTimeInMillis());
                setResult(Activity.RESULT_OK, intent);
                finish();


            }
        });

    }
}
