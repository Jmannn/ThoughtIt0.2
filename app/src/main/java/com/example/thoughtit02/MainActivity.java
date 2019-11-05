package com.example.thoughtit02;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity{
    private static final String TAG = "MainActivity";

    private List<String> thoughts = new ArrayList<>();
    private List<Date> dates = new ArrayList<>();
    private RecyclerViewAdaptor adaptor;
    private EditText textBox;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreatet start");
        initThoughts();
        this.textBox = findViewById(R.id.text_enter);
    }
    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "- ON PAUSE -");
    }
    //use this for model loading of prev thoughts empty on first open
    private void initThoughts(){
        Log.d(TAG, "Creating thoughts");
        String str = "";

        initRecyclerView();
    }
    private void initRecyclerView(){
        Log.d(TAG,  "initRecyclerView: init recyclerview.");
        this.recyclerView = findViewById(R.id.recycler_view);
        this.adaptor = new RecyclerViewAdaptor(this, thoughts, dates);
        recyclerView.setAdapter(adaptor);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.hasFixedSize();
    }
    public void saveThought(View view){
        Log.d(TAG,"saveThought");
        this.thoughts.add(this.textBox.getText().toString());
        this.textBox.setText("");
        this.dates.add(new Date());
        this.adaptor.notifyItemInserted(this.thoughts.size()-1);
        this.recyclerView.scrollToPosition(this.thoughts.size()-1);

    }
}
//TODO: Only display the thoughts of each day.
//TODO: Have a toolbar drop down that can choose the date to display
// but if the dates dont match you cant add, maybe hide components
// multiple drop downs which have columns for year, month and day
