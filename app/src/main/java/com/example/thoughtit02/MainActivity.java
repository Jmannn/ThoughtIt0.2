package com.example.thoughtit02;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity{
    private static final String TAG = "MainActivity";

    DatabaseHelper mDatabaseHelper;
    private List<String> thoughts = new ArrayList<>();
    private List<Date> dates = new ArrayList<>();
    private List<String> type = new ArrayList<>();
    private List<String> url = new ArrayList<>();
    private RecyclerViewAdaptor adaptor;
    private EditText editBox;
    private RecyclerView recyclerView;
    private SharedPreferences sharedPreferences;
    private final String dateKey = "Dates";
    private final String thoughtKey = "Thoughts";
    private Date currentMinDate;
    private Date currentMaxDate;
    //only used for checking if the first date has been picked
    private boolean selectMaxDate = false;
    private Toolbar toolbar;
    private Activity activity = (MainActivity.this);
    private ConstraintLayout constraintLayout;
    protected int redoPosition;
    protected String redoThought;
    protected Date redoDate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate start");
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        sharedPreferences = getSharedPreferences("USER", MODE_PRIVATE);
        this.mDatabaseHelper = new DatabaseHelper(this);
        this.currentMinDate = getYesterday();
        this.currentMaxDate = new Date();
        initThoughts();
        this.editBox = findViewById(R.id.text_enter);
        this.constraintLayout = findViewById(R.id.constraint_layout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }
    public void pickDate(){
        Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
        startActivityForResult(intent, 1);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.select_date_range:{
                Log.d("DEBUG", "selectDateRangee");
                pickDate();
                break;
            }
            case R.id.set_current_date:{
                Log.d("DEBUG", "set to currentdate");
                getDataInRange(getYesterday(), new Date());
                this.adaptor.notifyDataSetChanged();
                this.recyclerView.scrollToPosition(this.thoughts.size()-1);
                break;
            }
            case R.id.clear:{
                mDatabaseHelper.clearDatabase();
            }
        }
        return super.onOptionsItemSelected(item);
    }
    //used to return from calendarview
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                long result=data.getLongExtra("date", -1);
                if(this.selectMaxDate){
                    this.selectMaxDate = false;
                    this.currentMaxDate = new Date(result);
                    Log.d("DEBUG", "++"+this.currentMaxDate.toString());
                    getDataInRange(this.currentMinDate, this.currentMaxDate);


                } else {
                    this.selectMaxDate = true;
                    this.currentMinDate = new Date(result);
                    Log.d("DEBUG", "++"+this.currentMinDate.toString());
                    pickDate();

                }
                Log.d("DEBUG", new Date(result).toString());
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    //use this for model loading of prev thoughts empty on first open
    private void initThoughts(){
        Log.d(TAG, "Creating thoughts");
        String str = "";

        /////////////////////////////Heree

        getDataInRange(this.currentMinDate, this.currentMaxDate);

        ///..........................DB QUERY FROM STart of yesterday

        initRecyclerView();
    }
    /* Used to compute yesterdays date and return it as a date object.
        @return the date object containing yesterdays date
     */
    private Date getYesterday(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        return calendar.getTime();
    }

    private void initRecyclerView(){
        Log.d(TAG,  "initRecyclerView: init recyclerview.");
        this.recyclerView = findViewById(R.id.recycler_view);
        this.adaptor = new RecyclerViewAdaptor(this, thoughts, dates);
        recyclerView.setAdapter(adaptor);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.hasFixedSize();
        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new SwipeToDeleteCallback(this.adaptor));
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }
    public void saveThought(View view){
        Log.d(TAG,"saveThought");
        Date date = new Date();

        String thought = this.editBox.getText().toString();
        if(thought.isEmpty()) return;

        this.thoughts.add(thought);
        this.editBox.setText("");
        this.dates.add(date);
        this.adaptor.notifyItemInserted(this.thoughts.size()-1);
        this.recyclerView.scrollToPosition(this.thoughts.size()-1);
        boolean insertData = mDatabaseHelper.addData(date.getTime(),thought,"Text","");

        if(insertData ==false){
            toastMessage("Could not insert thought.");
        } else {
            toastMessage("Created thought");
        }

    }

    public void searchThought(View view){
        //Search button
        EditText editText = findViewById(R.id.search_bar);
        String searchStr = editText.getText().toString();
        editText.setText("");
        displayData(mDatabaseHelper.searchData(searchStr));

    }
    //TODO: this will have a range
    public void getDataInRange(Date lower, Date upperBound){
        long lowerBound;
        Calendar cal = Calendar.getInstance();
        cal.setTime(lower);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        lowerBound = cal.getTimeInMillis();
        ///
        cal.setTime(upperBound);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        displayData(mDatabaseHelper.getData(lowerBound, cal.getTimeInMillis()));
    }
    private void displayData(Cursor cursor){
        this.dates.clear();
        this.thoughts.clear();
        this.type.clear();
        this.url.clear();
        while(cursor.moveToNext()){
            this.dates.add(new Date(cursor.getLong(0)));
            this.thoughts.add(cursor.getString(1));
            this.type.add(cursor.getString(2));
            this.url.add(cursor.getString(3));
        }
        if(this.adaptor != null) {
            this.adaptor.notifyDataSetChanged();
            this.recyclerView.scrollToPosition(this.thoughts.size() - 1);
        }
    }
    public void removeThought(int pos){
        boolean result = mDatabaseHelper.removeDatum(this.dates.get(pos).getTime());

        if(result){
            this.redoThought = this.thoughts.get(pos);
            this.redoDate = this.dates.get(pos);
            this.redoPosition = pos;

            this.thoughts.remove(pos);
            this.dates.remove(pos);
            this.adaptor.notifyItemRemoved(pos);
            final Snackbar snackbar = Snackbar.make(constraintLayout, "Removed: " + this.redoThought, Snackbar.LENGTH_LONG)
                    .setAction("UNDO", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            redo();
                        }
                    });
            snackbar.show();
        } else {
            toastMessage("'"+thoughts.get(pos) + "' could not be removed.");
        }

    }
    public void redo(){
        this.thoughts.add(this.redoPosition,this.redoThought);
        this.dates.add(this.redoPosition,this.redoDate);
        this.adaptor.notifyItemInserted(this.redoPosition);
        this.recyclerView.scrollToPosition(this.thoughts.size()-1);
        Snackbar snackbar = Snackbar.make(constraintLayout, "Undo Success",Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    public void toastMessage(String text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
    /* This method brings up a menu containing options regarding input
     * such as image, video, audio. Responds to a '+' button click.
     * @param the view object
     */
    public void thoughtOptions(View view) {
        Log.d("DEBUG", "thoughtOptions");
        View v = findViewById(R.id.thought_options);
        PopupMenu pm = new PopupMenu(MainActivity.this, v);
        pm.getMenuInflater().inflate(R.menu.input_options, pm.getMenu());
        pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.clear_input:
                        editBox.setText("");
                        break;
                    case R.id.add_photo:
                        break;
                    case R.id.redo:
                        redo();
                        break;
                }

                return true;
            }
        });
        pm.show();
    }
    public void updateThought(int position, EditText editText){
        this.thoughts.set(position, editText.getText().toString());
    }
}

//TODO: Highlight notes
//TODO: image with caption, a plus button that with a menu that allows you to attach images

