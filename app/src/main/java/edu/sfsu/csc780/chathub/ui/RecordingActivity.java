package edu.sfsu.csc780.chathub.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Application;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;

import edu.sfsu.csc780.chathub.R;
import edu.sfsu.csc780.chathub.model.ChatMessage;

/**
 * Created by Dip on 12/5/2016.
 */

public class RecordingActivity extends AppCompatActivity {

    private static final String TAG = RecordingActivity.class.getSimpleName();
    private static MediaRecorder mRecorder;
    private static String mFileName = null;

    public static StorageReference mStorageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        mStorageReference = FirebaseStorage.getInstance().getReference();
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName = "/recorded_audio.3gp";

    }



    public void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);


                try {
                    mRecorder.prepare();
                } catch (IOException e) {
                    Log.e(TAG, "prepare() failed");
                }
                mRecorder.start();
                Log.i("Recording started","OK");




    }

   public  void stopRecording() {

               mRecorder.stop();
               mRecorder.release();
               mRecorder = null;

           }



    }










