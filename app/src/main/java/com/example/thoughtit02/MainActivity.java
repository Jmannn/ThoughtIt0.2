package com.example.thoughtit02;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.media.MediaPlayer.SEEK_CLOSEST;

public class MainActivity extends AppCompatActivity {
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

    private int redoPosition;
    private String redoThought;
    private Date redoDate;
    private String redoUri;
    private String redoType;

    static final int  CAMERA_REQUEST_CODE = 1;
    private Uri picUri;

    private MediaPlayer mediaPlayer;
    private int currentDuration;
    private String currentAudio;
    private int currentAudioPosition;

    private Handler seekHandler;



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
        //alter this
        int YOUR_REQUEST_CODE = 200; // could be something else..
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) //check if permission request is necessary
        {
            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.RECORD_AUDIO,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, YOUR_REQUEST_CODE);
        }
        this.mediaPlayer = new MediaPlayer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }
    @Override
    public void onPause(){
        super.onPause();
        this.mediaPlayer.release();
    }

    public void pickDate(){
        Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
        startActivityForResult(intent, 666);
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            if (photoFile != null) {

                 picUri = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, picUri);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d("DEBUG","RESUME");
        this.adaptor = null;//must restart
        initThoughts();
        this.mediaPlayer = new MediaPlayer();
        this.mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer player) {
                player.start();
                seekHandler = new Handler();
                seekHandler.post(updateSeekBarTime);


            }

        });
        this.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.seekTo(0);

            }

        });/*this.mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) {
                Log.d("DEBUG","Seek Complete. Current Position: " + mp.getCurrentPosition());
                mp.start();
            }
        });*/
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        return image;
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
                this.recyclerView.scrollToPosition(-1);
                break;
            }
            case R.id.clear:{
                mDatabaseHelper.clearDatabase();
            }
        }
        return super.onOptionsItemSelected(item);
    }
    //used to return from calendarviewc
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 666) {
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
        else if(requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK){
            Log.d("DEBUG", "PHOTO TAKEN ");
            Date date = new Date();

            String thought = this.editBox.getText().toString();
            //if(thought.isEmpty()) return;
            Uri uri = picUri;
            this.thoughts.add(thought);
            this.editBox.setText("");
            this.dates.add(date);
            this.type.add("Photo");
            this.url.add(uri.toString());
            this.adaptor.notifyItemInserted(this.thoughts.size()-1);
            boolean insertData = mDatabaseHelper.addData(date.getTime(),thought,"Photo",uri.toString());
            this.recyclerView.scrollToPosition(this.thoughts.size()-1);
        }
        else if (requestCode == 777 && resultCode == RESULT_OK){
            String thought = this.editBox.getText().toString();
            String recordingUri = data.getStringExtra("recordingUri");
            Log.d("DEBUG", "Redcording Done "+ recordingUri);
            Date date = new Date();
            this.thoughts.add(thought);
            this.editBox.setText("");
            this.dates.add(date);
            this.type.add("Audio");
            this.url.add(recordingUri);
            this.adaptor.notifyItemInserted(this.thoughts.size()-1);
            boolean insertData = mDatabaseHelper.addData(date.getTime(),thought,"Audio",recordingUri);
            this.recyclerView.scrollToPosition(this.thoughts.size()-1);

        }
    }

    /* This is a callback function which is called when A MediaPlayer
     * instance has finished loading a recording to play. It also calls
     */
    private void startRecordActivity(){
        Intent intent = new Intent(this, RecordActivity.class);
        startActivityForResult(intent, 777);
    }
    private Runnable updateSeekBarTime = new Runnable() {
        double newBarPosition = 0;
        public void run() {
            Log.d("DEBUG", "run: sdfsdfsdfsdfsdf");
            newBarPosition = ((double) mediaPlayer.getCurrentPosition() / (double) mediaPlayer.getDuration()) * 100;
            //all we need to now is update the seekbar
            Log.d("DEBUG", "run: "+ newBarPosition);
            adaptor.notifyItemChanged(currentAudioPosition , (int)newBarPosition);

            seekHandler.postDelayed(updateSeekBarTime, 1000);
        }
    };
    public void playRecording(int pos){
        Log.d("DEBUG", "playRecording " +this.type.get(pos)+" "+this.url.get(pos));

        if (this.url.get(pos).equals(this.currentAudio)){
            Log.d("DEBUG", "playRecording: resume " +this.currentDuration);

            //this.mediaPlayer.seekTo(this.currentDuration);
            this.adaptor.notifyItemChanged(pos , 50);
            this.mediaPlayer.start();
        } else {
            try {
                this.currentAudioPosition = pos;
                this.currentAudio = this.url.get(pos);
                this.mediaPlayer.reset();
                this.mediaPlayer.setDataSource(this.url.get(pos));
                this.mediaPlayer.prepareAsync(); // prepare async to not block main thread



            } catch (IOException e) {
                Toast.makeText(this, "audio file not found", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }
    public void timePostionOfTrack(int position, int progress){
        Log.d("DEBUG", "timePostionOfTrack: "+progress);
        double fractionOfTotal = progress/100.0;

        Log.d("DEBUG", ""+fractionOfTotal);
        int newTime = (int) (fractionOfTotal * this.mediaPlayer.getDuration());
        Log.d("DEBUG", newTime+"/"+this.mediaPlayer.getDuration());
        this.mediaPlayer.pause();
        this.mediaPlayer.seekTo(newTime);
        this.mediaPlayer.start();
    }
    public void pauseRecording(){
        Log.d("DEBUG", "Pause at: "+this.mediaPlayer.getCurrentPosition());
        if(mediaPlayer.isPlaying()) {
            this.mediaPlayer.pause();
            this.currentDuration = this.mediaPlayer.getCurrentPosition();
        }
    }
    public void stopRecording(){
        Log.d("DEBUG", "Stopped and reset audio");
        if( mediaPlayer.isPlaying()) {
            this.mediaPlayer.pause();
            this.mediaPlayer.seekTo(0);
        } else if (mediaPlayer.getCurrentPosition()>0){
            this.mediaPlayer.seekTo(0);
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
        this.adaptor = new RecyclerViewAdaptor(this, thoughts, dates, type, url);
        recyclerView.setAdapter(adaptor);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.hasFixedSize();
        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new SwipeToDeleteCallback(this.adaptor));
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }
    /*Saves a plaintext thougth */
    public void saveThought(View view){
        Log.d(TAG,"saveThought");
        String type = "Text";
        String urlEmpty = "";
        Date date = new Date();


        String thought = this.editBox.getText().toString();
        if(thought.isEmpty()) return;

        this.thoughts.add(thought);
        this.editBox.setText("");
        this.dates.add(date);
        this.type.add(type);
        this.url.add(urlEmpty);
        this.adaptor.notifyItemInserted(this.thoughts.size()-1);
        this.recyclerView.scrollToPosition(this.thoughts.size()-1);
        boolean insertData = mDatabaseHelper.addData(date.getTime(),thought,type,urlEmpty);

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
            this.redoType = this.type.get(pos);
            this.redoUri = this.url.get(pos);
            this.redoPosition = pos;

            this.thoughts.remove(pos);
            this.dates.remove(pos);
            this.type.remove(pos);
            this.url.remove(pos);
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
        if (this.redoThought == null || this.redoType == null
        || this.redoDate == null || this.redoUri == null){
            toastMessage("Nothing to redo!");
            return;
        }
        this.thoughts.add(this.redoPosition,this.redoThought);
        this.dates.add(this.redoPosition,this.redoDate);
        this.adaptor.notifyItemInserted(this.redoPosition);
        this.recyclerView.scrollToPosition(this.thoughts.size()-1);
        this.mDatabaseHelper.addData(redoDate.getTime(), redoThought, redoType, redoUri);
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
                        dispatchTakePictureIntent();
                        break;
                    case R.id.redo:
                        redo();
                        break;
                    case R.id.record_memo:
                        startRecordActivity();
                        break;
                }

                return true;
            }
        });
        pm.show();
    }
    public void updateThought(int position, EditText editText){
        String editedThought = editText.getText().toString();
        this.thoughts.set(position, editedThought);
        boolean result = this.mDatabaseHelper.updateData(editedThought, this.dates.get(position).getTime());
        if (result){
            toastMessage("Thought edit SUCCESS");
        } else {
            toastMessage("Thought edit not successful");
        }
    }

}

//Add buttons to activity for play
//TODO: DISABLE swipe to delete when sound playing
//