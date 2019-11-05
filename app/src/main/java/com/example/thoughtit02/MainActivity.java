package com.example.thoughtit02;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private List<String> thoughts = new ArrayList<>();
    private List<Date> dates = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreatet start");
        initThoughts();
    }
    private void initThoughts(){
        Log.d(TAG, "Creating thoughts");
        String str = "";
        for (int i = 0; i < 10; i++) {
            str = "This is the news " + i + " " + i * i;
            str += " sdfsdfsdfsdfkjsdf;kajdhfk;djfgs;dkjfgnsd;fk;kgndsfg;sdnfg;sjndfg;jsndfg;jnsfg;jnsdfg;sdfg;sdfg;kjsdfgkj";
            str += "dfasdfhasdfkjsdflkjhasdflkjhasddflkjhasdf";
            dates.add(new Date());
            thoughts.add(str);
        }
        initRecyclerView();
    }
    private void initRecyclerView(){
        Log.d(TAG,  "initRecyclerView: init recyclerview.");
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        RecyclerViewAdaptor adaptor = new RecyclerViewAdaptor(this, thoughts, dates);
        recyclerView.setAdapter(adaptor);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.hasFixedSize();
    }
}
