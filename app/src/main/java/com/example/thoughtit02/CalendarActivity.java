package com.example.thoughtit02;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.CalendarView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class CalendarActivity extends AppCompatActivity {
    private final String TAG = "CalendarActivity";
    private CalendarView mCalendarView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_layout);
        this.mCalendarView = findViewById(R.id.calendarView);

        this.mCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month,dayOfMonth);


                Intent intent = new Intent(CalendarActivity.this, MainActivity.class);
                intent.putExtra("date", calendar.getTimeInMillis());
                setResult(Activity.RESULT_OK, intent);
                finish();


            }
        });

    }
}
