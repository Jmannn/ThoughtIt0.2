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

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    DatabaseHelper mDatabaseHelper;
    private RecyclerViewAdaptor adaptor;
    private EditText editBox;
    private RecyclerView recyclerView;
    private Date currentMinDate;
    private Date currentMaxDate;
    private boolean selectMaxDate = false;
    private Toolbar toolbar;
    private ConstraintLayout constraintLayout;

    private int redoPosition;
    private String redoThought;
    private Date redoDate;
    private String redoUri;
    private String redoType;

    static final int  CAMERA_REQUEST_CODE = 1;
    static final int  CALENDAR_REQUEST_CODE = 2;
    static final int  AUDIO_REQUEST_CODE = 3;
    private Uri picUri;

    private MediaPlayer mediaPlayer;
    private int currentDuration;
    private String currentAudio;
    private int currentAudioPosition;

    private Handler seekHandler;

    private ThoughtCollection thoughtCollection;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate start");
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        this.mDatabaseHelper = new DatabaseHelper(this);
        this.currentMinDate = getYesterday();
        this.currentMaxDate = new Date();
        initThoughts();
        this.editBox = findViewById(R.id.text_enter);
        this.constraintLayout = findViewById(R.id.constraint_layout);
        int YOUR_REQUEST_CODE = 200;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
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
        startActivityForResult(intent, CALENDAR_REQUEST_CODE);
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
        Log.d(TAG,"RESUME");
        this.adaptor = null;
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

        });
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return image;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.select_date_range:{
                pickDate();
                break;
            }
            case R.id.set_current_date:{
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CALENDAR_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                long result=data.getLongExtra("date", -1);
                if(this.selectMaxDate){
                    this.selectMaxDate = false;
                    this.currentMaxDate = new Date(result);
                    getDataInRange(this.currentMinDate, this.currentMaxDate);
                } else {
                    this.selectMaxDate = true;
                    this.currentMinDate = new Date(result);
                    pickDate();

                }

            }
            if (resultCode == Activity.RESULT_CANCELED) {
                toastMessage("Could not complete task!");
            }
        }
        else if(requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK){
            Date date = new Date();
            String thoughtText = this.editBox.getText().toString();
            Uri uri = picUri;
            Thought thought = new Thought(date, thoughtText, Type.PICTURE, uri.toString());
            this.thoughtCollection.addThought(thought);
            this.editBox.setText("");
            this.adaptor.notifyItemInserted(this.thoughtCollection.getDisplaySize()-1);
            this.recyclerView.scrollToPosition(this.thoughtCollection.getDisplaySize()-1);
        }
        else if (requestCode == AUDIO_REQUEST_CODE && resultCode == RESULT_OK){
            String thoughtText = this.editBox.getText().toString();
            String recordingUri = data.getStringExtra("recordingUri");
            Date date = new Date();
            Thought thought = new Thought(date, thoughtText, Type.AUDIO, recordingUri);
            this.thoughtCollection.addThought(thought);
            this.editBox.setText("");
            this.adaptor.notifyItemInserted(this.thoughtCollection.getDisplaySize()-1);
            this.recyclerView.scrollToPosition(this.thoughtCollection.getDisplaySize()-1);

        }
    }

    /* This is a callback function which is called when A MediaPlayer
     * instance has finished loading a recording to play. It also calls
     */
    private void startRecordActivity(){
        Intent intent = new Intent(this, RecordActivity.class);
        startActivityForResult(intent, AUDIO_REQUEST_CODE);
    }
    private Runnable updateSeekBarTime = new Runnable() {
        double newBarPosition = 0;
        public void run() {
            newBarPosition = ((double) mediaPlayer.getCurrentPosition() / (double) mediaPlayer.getDuration()) * 100;
            adaptor.notifyItemChanged(currentAudioPosition , (int)newBarPosition);
            seekHandler.postDelayed(updateSeekBarTime, 1000);
        }
    };
    public void playRecording(int pos){
        Thought thought = this.thoughtCollection.getThought(pos);
        String uri = thought.getUri();
        if (uri.equals(this.currentAudio)){
            this.adaptor.notifyItemChanged(pos , 50);
            this.mediaPlayer.start();
        } else {
            try {
                this.currentAudioPosition = pos;
                this.currentAudio = uri;
                this.mediaPlayer.reset();
                this.mediaPlayer.setDataSource(uri);
                this.mediaPlayer.prepareAsync();
            } catch (IOException e) {
                Toast.makeText(this, "audio file not found", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }
    public void timePositionOfTrack(int position, int progress){
        final double oneHundredPercent = 100.0;
        double fractionOfTotal = progress/oneHundredPercent;
        int newTime = (int) (fractionOfTotal * this.mediaPlayer.getDuration());
        this.mediaPlayer.pause();
        this.mediaPlayer.seekTo(newTime);
        this.mediaPlayer.start();
    }
    public void pauseRecording(){
        if(mediaPlayer.isPlaying()) {
            this.mediaPlayer.pause();
            this.currentDuration = this.mediaPlayer.getCurrentPosition();
        }
    }
    public void stopRecording(){
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
        getDataInRange(this.currentMinDate, this.currentMaxDate);
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
        this.adaptor = new RecyclerViewAdaptor(this, thoughtCollection);
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
        String urlEmpty = "";
        String thoughtText = this.editBox.getText().toString();
        Thought thought = new Thought(new Date(), thoughtText, Type.TEXT, urlEmpty);

        if(thoughtText.isEmpty()) return;
        boolean insertData = this.thoughtCollection.addThought(thought);
        this.editBox.setText("");
        this.adaptor.notifyItemInserted(this.thoughtCollection.getDisplaySize()-1);
        this.recyclerView.scrollToPosition(this.thoughtCollection.getDisplaySize()-1);

        if(!insertData){
            toastMessage("Could not insert thought.");
        } else {
            toastMessage("Created thought");
        }

    }

    public void searchThought(View view){
        EditText editText = findViewById(R.id.search_bar);
        String searchStr = editText.getText().toString();
        editText.setText("");
        displayData(mDatabaseHelper.searchData(searchStr));
    }
    public void getDataInRange(Date lower, Date upperBound){
        long lowerBound;
        Calendar cal = Calendar.getInstance();
        cal.setTime(lower);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        lowerBound = cal.getTimeInMillis();

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
        boolean result = this.thoughtCollection.removeThought(pos);

        if(result){

            this.adaptor.notifyItemRemoved(pos);
            final Snackbar snackbar = Snackbar.make(constraintLayout, "Removed: "
                    + this.thoughtCollection.getRedo().getThoughtText(), Snackbar.LENGTH_LONG)
                    .setAction("UNDO", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            redo();
                        }
                    });
            snackbar.show();
        } else {
            toastMessage(" Could not remove thought !");
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
        boolean result = this.thoughtCollection.updateThought(position, editedThought);
        if (result){
            toastMessage("Thought edit SUCCESS");
        } else {
            toastMessage("Thought edit not successful");
        }
    }

}