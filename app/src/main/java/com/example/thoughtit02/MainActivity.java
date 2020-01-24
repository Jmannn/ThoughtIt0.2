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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import java.util.Date;
import java.util.Locale;
/* This is the main activity of my application.
 * Most of the GUI and application are controlled from here.
 * Audio is also played from this thread.
 * @Johnny Mann
 */
public class MainActivity extends AppCompatActivity {
    /* Log tag. */
    private static final String TAG = "MainActivity";
    /* The adaptor for main recyclerview. */
    private RecyclerViewAdaptor adaptor;
    /* The edit box at the bottom of the screen. */
    private EditText editBox;
    /* Reference to the main recyclerview. */
    private RecyclerView recyclerView;
    /* The current maximum date being displayed. */
    private Date currentMinDisplayDate;
    /* The current minimum date being displayed. */
    private Date currentMaxDisplayDate;
    /* The layout containing the main GUI. */
    private ConstraintLayout constraintLayout;
    /* Request ID for various intents. */
    static final int  CAMERA_REQUEST_CODE = 1;
    static final int  CALENDAR_REQUEST_CODE_LOWER_BOUND = 2;
    private static final int CALENDAR_REQUEST_CODE_UPPPER_BOUND = 4;
    static final int  AUDIO_REQUEST_CODE = 3;
    /* Uri for pic returned by photo intent. */
    private Uri picUri;
    /* Media player for playing various audio in current View. */
    private MediaPlayer mediaPlayer;
    /* The URI of the file currently being played by mediaplayer. */
    private String currentAudioUri;
    /* The ms elapsed time of MediaPlayer. */
    private int currentAudioPosition;
    /* The background thread which updates the various seekbars in the recycler view. */
    private Handler seekHandler;
    /* The storage and model for the thoughts. */
    private ThoughtCollection thoughtCollection;

    /* Started on creation of instance. Sets up the application to display the
     * most recent thoughts.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate start");
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        this.currentMinDisplayDate = Utilities.getYesterday();
        this.currentMaxDisplayDate = new Date();
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
    /* Inflates toolbar options menu.
     * @param always returns true on completion */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }
    /* On pause, the application release the MediaPlayer and removes
     * the last file in the redo from the file system.
     */
    @Override
    public void onPause(){
        super.onPause();
        this.mediaPlayer.release();
        this.thoughtCollection.removeLastFile();
    }
    /* Starts the calendar intent to allow the user to pick
     * a date.
     */
    public void pickDate(int requestCode, long lowerBound, long upperBound){
        final String LOWER_BOUND_ID = "LOWER_DATE";
        final String UPPER_BOUND_ID = "UPPER_DATE";
        Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
        intent.putExtra(LOWER_BOUND_ID, lowerBound);
        intent.putExtra(UPPER_BOUND_ID, upperBound);
        startActivityForResult(intent, requestCode);
    }
    /* Prepares a file and opens up the camera so user can take a picture. */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.d(TAG, "dispatchTakePictureIntent: Could not create image file!");
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
    /* Reinitializes the displayed information, as well as the MediaPlayer. */
    @Override
    public void onResume(){
        final int start = 0;
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
                mediaPlayer.seekTo(start);
            }

        });
    }
    /* Creates an image file and uses a timestamp to uniquely name it.
     * @return a file with a timestamp as a name
     */
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).toString();
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }
    /* This method handles clicks to the toolbar menu.
     * @param the menu item being clicked
     * @return whether to pass on the event or consume it here
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.select_date_range:{
                pickDate(CALENDAR_REQUEST_CODE_LOWER_BOUND, thoughtCollection.smallestDate(),
                        thoughtCollection.largestDate());
                break;
            }
            case R.id.set_current_date:{
                this.thoughtCollection.prepareDataSet(Utilities.getYesterday(), new Date());
                if(this.adaptor != null) {
                    notifyDataSetChangedAndScrollToBottom();
                }
                break;
            }
            case R.id.clear:{
                clearAlertDialog();
            }
        }
        return super.onOptionsItemSelected(item);
    }
    /* This method should be called whenever a user would like to
       remove any thoughts from the database. This method prompts them
       giving them time to decide and preventing accidental deletes.
     */
    public void clearAlertDialog() {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete All Thoughts!");
        builder.setMessage("Are you sure you would like to delete all thoughts?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                clearThoughts();
            }
        });

        builder.setNegativeButton("Cancel", null);

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    /* This clears the thoughts from the thought collection and then notifies
     * the recyclerview.
     */
    public void clearThoughts(){
        this.thoughtCollection.clearThoughts();
        this.adaptor.notifyDataSetChanged();
    }
    /* This listener waits for activities CalendarView, Camera, or Audio record to return.
     * It then adds the thought to the database and updates recyclerview. If it is calendarview,
     * it displays the dates chosen.
     * @param The intent which is being recieved.
     * @param Whether it was returned correctly
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CALENDAR_REQUEST_CODE_LOWER_BOUND && resultCode == Activity.RESULT_OK){
             long result=data.getLongExtra(getString(R.string.DateIntentID), -1);
             this.currentMinDisplayDate = new Date(result);
             pickDate(CALENDAR_REQUEST_CODE_UPPPER_BOUND, result ,this.thoughtCollection.largestDate());
        } else if (requestCode == CALENDAR_REQUEST_CODE_UPPPER_BOUND && resultCode == Activity.RESULT_OK){
            long result=data.getLongExtra(getString(R.string.DateIntentID), -1);
            this.currentMaxDisplayDate = new Date(result);
            this.thoughtCollection.prepareDataSet(this.currentMinDisplayDate, this.currentMaxDisplayDate);
        } else if(requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK){
            Date date = new Date();
            String thoughtText = this.editBox.getText().toString();
            Uri uri = picUri;
            Log.d(TAG, "onActivityResult: "+date.getTime());
            Thought thought = new Thought(date, thoughtText, Type.PICTURE, uri.toString());

            this.thoughtCollection.addThought(thought);
            this.editBox.setText("");
            notifyAdaptorAndScrollToBottom();
        }
        else if (requestCode == AUDIO_REQUEST_CODE && resultCode == RESULT_OK){
            String thoughtText = this.editBox.getText().toString();
            String recordingUri = data.getStringExtra(getString(R.string.RecordingUriIntentID));
            Date date = new Date();
            Thought thought = new Thought(date, thoughtText, Type.AUDIO, recordingUri);
            this.thoughtCollection.addThought(thought);
            this.editBox.setText("");
            notifyAdaptorAndScrollToBottom();

        } else if (resultCode == Activity.RESULT_CANCELED) {
            toastMessage("Could not complete task!");
        }
    }

    /* This method starts the record activity, allows user to record audio. */
    private void startRecordActivity(){
        Intent intent = new Intent(this, RecordActivity.class);
        startActivityForResult(intent, AUDIO_REQUEST_CODE);
    }
    /* A runnable used to update the seekbars in recycler view.
     * Converts elapsed time and total time of media player to a percentage.*/
    private Runnable updateSeekBarTime = new Runnable() {
        final int delayUpdateTimeMS = 1000;
        final int oneHundredPercent = 100;
        double newBarPosition;
        public void run() {
            newBarPosition = ((double) mediaPlayer.getCurrentPosition()
                    / (double) mediaPlayer.getDuration()) * oneHundredPercent;
            adaptor.notifyItemChanged(currentAudioPosition , (int)newBarPosition);
            seekHandler.postDelayed(updateSeekBarTime, delayUpdateTimeMS);
        }
    };
    /* This initiates the process of playing a recording, or it continues playing a
     * paused recording.
     * @param the position of file in both RecyclerView and collection model
     */
    public void playRecording(int pos){
        Thought thought = this.thoughtCollection.getThought(pos);
        String uri = thought.getUri();
        if (uri.equals(this.currentAudioUri)){
            this.mediaPlayer.start();
        } else {
            try {
                this.currentAudioPosition = pos;
                this.currentAudioUri = uri;
                this.mediaPlayer.reset();
                this.mediaPlayer.setDataSource(uri);
                this.mediaPlayer.prepareAsync();
            } catch (IOException e) {
                Toast.makeText(this, "audio file not found", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }
    /* Changes the elapsed time of a track. Allows for jumping
     * between various parts of the track.
     * @param the time to jump to as a percentage of the overall time
     */
    public void changeTimePositionOfTrack(int progress){
        final double oneHundredPercent = 100.0;
        double fractionOfTotal;
        int newTime;
        fractionOfTotal = progress/oneHundredPercent;
        newTime = (int) (fractionOfTotal * this.mediaPlayer.getDuration());
        this.mediaPlayer.pause();
        this.mediaPlayer.seekTo(newTime);
        this.mediaPlayer.start();
    }
    /* If the recording is playing, it will become paused. */
    public void pauseRecording(){
        if(mediaPlayer.isPlaying()) {
            this.mediaPlayer.pause();
        }
    }
    /* Pauses the recording and sets elapsed time at 0. */
    public void stopRecording(){
        final int beginningMS = 0;
        if( mediaPlayer.isPlaying()) {
            this.mediaPlayer.pause();
            this.mediaPlayer.seekTo(beginningMS);
        } else if (mediaPlayer.getCurrentPosition()>beginningMS){
            this.mediaPlayer.seekTo(beginningMS);
        }
    }
    /* Initializes thoughts and then starts the RecyclerView.*/
    private void initThoughts(){
        Log.d(TAG, "Creating thoughts");
        this.thoughtCollection = new ThoughtCollection(this);
        this.thoughtCollection.prepareDataSet(this.currentMinDisplayDate, this.currentMaxDisplayDate);
        initRecyclerView();
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
    /*Saves a plaintext thought. */
    public void saveThought(View view){
        Log.d(TAG,"saveThought");
        String urlEmpty = "";
        String thoughtText = this.editBox.getText().toString();
        Thought thought = new Thought(new Date(), thoughtText, Type.TEXT, urlEmpty);

        if(thoughtText.isEmpty()) return;
        boolean insertData = this.thoughtCollection.addThought(thought);
        this.editBox.setText("");
        notifyAdaptorAndScrollToBottom();

        if(!insertData){
            toastMessage("Could not insert thought.");
        } else {
            toastMessage("Created thought");
        }

    }
    /* Searches for a particular string in a thought and displays result in
     * RecyclerView.
     * @param the view click it is responding too
     */
    public void searchThought(View view){
        EditText editText = findViewById(R.id.search_bar);
        String searchStr = editText.getText().toString();
        editText.setText("");
        this.thoughtCollection.searchAndDisplay(searchStr);
        notifyDataSetChangedAndScrollToBottom();
    }
    /* Removes a thought from both the database and the RecyclerView. It displays a pop which allows
     * a user to redo this.
     * @param the position of the thought in both the RecyclerView and model
     */
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
    /* Adds back a previously removed thought. */
    public void redo(){
        boolean redo = this.thoughtCollection.redo();
        if(redo) {
            Snackbar snackbar = Snackbar.make(constraintLayout, "Undo Success", Snackbar.LENGTH_SHORT);
            snackbar.show();
            this.adaptor.notifyItemInserted(this.thoughtCollection.getRedoPosition());
        } else {
            toastMessage("Nothing to redo!");
        }
    }
    /* Shows a message to the user.
     * @param the text to display
     */
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
    /* Updates a thought that is both in the database and RecyclerView.
     * @param position of the thought in both model and RecyclerView
     * @param edit text in the recycler view
     */
    public void updateThought(int position, EditText editText){
        String editedThought = editText.getText().toString();
        boolean result = this.thoughtCollection.updateThought(position, editedThought);
        if (result){
            toastMessage("Thought edit SUCCESS");
        } else {
            toastMessage("Thought edit not successful");
        }
    }
    /* Notifies the recycler view adaptor of single change and instructs it to scroll
     * to the bottom of the screen.
     */
    private void notifyAdaptorAndScrollToBottom(int pos){
        this.adaptor.notifyItemInserted(pos);
        this.recyclerView.scrollToPosition(this.thoughtCollection.getDisplaySize()-1);
    }
    private void notifyAdaptorAndScrollToBottom(){
        notifyAdaptorAndScrollToBottom(this.thoughtCollection.getDisplaySize()-1);
    }
    /* Notifies the recycler view adaptor of change and instructs it to scroll
     * to the bottom of the screen.
     */
    private void notifyDataSetChangedAndScrollToBottom(){
        this.adaptor.notifyDataSetChanged();
        this.recyclerView.scrollToPosition(this.thoughtCollection.getDisplaySize() - 1);
    }

}