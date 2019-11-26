package com.example.thoughtit02;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
/* This activity class is where a user can record audio memos.
 * save them and then return to main activity.
 * @author Johnny Mann
 */
public class RecordActivity extends AppCompatActivity {
    /* Save path for newly created audio. */
    String AudioSavePathInDevice = null;
    /* MediaRecorder for recording mic input. */
    MediaRecorder mediaRecorder ;
    /* Used for generating a fileName. */
    Random random ;
    /* For requesting storage. */
    public static final int RequestPermissionCode = 1;
    /* For user test playback of recording. */
    MediaPlayer mediaPlayer ;

    /* Sets up the view. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        random = new Random();

    }
    /* Request Storage and Audio Permission. */
    public void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, RequestPermissionCode);
    }
    /* Checks that user has granted permission. */
    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),
                WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(),
                RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED;
    }
    /* Responds to button click which starts recording process.
     * Requests permissions if they have not been requested already.
     * @param the view containing the button
     */
    public void startRecord(View view){
        if(checkPermission()) {
            AudioSavePathInDevice =
                    Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +
                            CreateTimeStamp() + getString(R.string.GenericAudioFileNameExtension);
            MediaRecorderReady();

            try {
                mediaRecorder.prepare();
                mediaRecorder.start();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            requestPermission();
        }
    }
    /* Plays a recording once recorded.
     * @param the view containing the button
     */
    public void play(View view){
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(AudioSavePathInDevice);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.start();
    }
    /* Stops the recording process and releases the MediaRecorder.
     * @param the view containing the button clicked
     */
    public void stopRecord(View view){
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
    }
    /* Prepares the MediaRecorder, and creates the file on disk. */
    public void MediaRecorderReady() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(AudioSavePathInDevice);
    }
    /* Creates a timestamp to be used as part of a filename.
     * @param the timestamp string
     */
    public String CreateTimeStamp(){
        @SuppressLint("SimpleDateFormat") String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return timeStamp;
    }
    /* Saves the audio recording and puts the URI in an
     * intent. Returns back to main activity.
     * @param the view containing the button clicked
     */
    public void save(View view){
        if(mediaPlayer!=null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if(mediaRecorder != null){
            mediaRecorder.release();
            mediaRecorder = null;
        }
        Intent intent = new Intent(RecordActivity.this, MainActivity.class);
        intent.putExtra(getString(R.string.RecordingUriIntentID), AudioSavePathInDevice);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}

