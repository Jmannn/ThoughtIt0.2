package com.example.thoughtit02;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
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
    private SharedPreferences sharedPreferences;
    private final String dateKey = "Dates";
    private final String thoughtKey = "Thoughts";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate start");
        sharedPreferences = getSharedPreferences("USER", MODE_PRIVATE);
        initThoughts();
        this.textBox = findViewById(R.id.text_enter);
    }
    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "- ON PAUSE -");
        //
        Gson gson = new Gson();
        String jsonThoughts = gson.toJson(this.thoughts);
        String jsonDates = gson.toJson(this.dates);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(this.thoughtKey, jsonThoughts);
        editor.putString(this.dateKey, jsonDates);
        editor.commit();
    }
    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "- ON STOP -");
    }
    //use this for model loading of prev thoughts empty on first open
    private void initThoughts(){
        Log.d(TAG, "Creating thoughts");
        String str = "";
        Gson gson = new Gson();
        String jsonThoughts = this.sharedPreferences.getString(this.thoughtKey, "");
        String jsonDates = this.sharedPreferences.getString(this.dateKey, "");
        Log.d("DEBUG", jsonThoughts);
        Log.d("DEBUG", jsonDates);
        if( !(jsonDates.isEmpty() && jsonThoughts.isEmpty()) ){
            //might have to run each date into the constructor
            TypeToken<List<String>> tokenThought = new TypeToken<List<String>>() {};
            TypeToken<List<Date>> tokenDate = new TypeToken<List<Date>>() {};
            this.thoughts = gson.fromJson(jsonThoughts, tokenThought.getType());
            this.dates = gson.fromJson(jsonDates, tokenDate.getType());
        }

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
    public void removeThought(int pos){
        Log.d(TAG, "removeThought : pos "+pos);
        this.thoughts.remove(pos);
        this.adaptor.notifyItemRemoved(pos);
    }
}
//TODO: Only display the thoughts of each day.
//TODO: Have a calander, select date, execute local db query
//TODO: On open, have little quite sound effect. One that a user can turn off
//TODO: So a calander and a menu