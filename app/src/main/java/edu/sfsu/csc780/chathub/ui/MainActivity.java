/**
 * Copyright Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.sfsu.csc780.chathub.ui;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.AsyncTaskLoader;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import edu.sfsu.csc780.chathub.R;
import edu.sfsu.csc780.chathub.model.ChatMessage;
import github.ankushsachdeva.emojicon.EmojiconGridView;
import github.ankushsachdeva.emojicon.EmojiconsPopup;
import github.ankushsachdeva.emojicon.emoji.Emojicon;

import static android.support.v4.app.RemoteInput.RESULTS_CLIP_LABEL;
import static edu.sfsu.csc780.chathub.R.id.time;


public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener,
        MessageUtil.MessageLoadListener ,MediaPlayer.OnPreparedListener{

    private static final String EXTRA_VOICE_REPLY = "extra_voice_reply";
    private static final String TAG = "MainActivity";
    public static final String MESSAGES_CHILD = "messages";
    private static final int REQUEST_INVITE = 1;
    public static final int REQUEST_PREFERENCES = 2;
    public static final int MSG_LENGTH_LIMIT = 64;
    private static final double MAX_LINEAR_DIMENSION = 500.0;
    public static final String ANONYMOUS = "anonymous";
    private static final int REQUEST_PICK_IMAGE = 1;
    public static final int REQUEST_TAKE_PHOTO = 4;
    private String mUsername;
    private String mPhotoUrl;
    private SharedPreferences mSharedPreferences;
    private GoogleApiClient mGoogleApiClient;

    private FloatingActionButton mSendButton;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;
    private EditText mMessageEditText;

    // Firebase instance variables
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private FirebaseRecyclerAdapter<ChatMessage, MessageUtil.MessageViewHolder>
            mFirebaseAdapter;
    private ImageButton mImageButton;
    private int mSavedTheme;
    private ImageButton mLocationButton;
    private ImageButton mCameraButton;
    private ImageButton mRecordButton;
    private MediaRecorder mRecorder;
    private String mFileName = null;
    private MediaPlayer mMediaplayer;
    public static String audioURL = null;

    public DatabaseReference ref;
    public static boolean isPaused;
    private Handler mHandler ;
    final Handler handler = new Handler();
    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        DesignUtils.applyColorfulTheme(this);
        setContentView(R.layout.activity_main);

        mHandler= new Handler();
        ref=FirebaseDatabase.getInstance().getReference();
        ref.child(MESSAGES_CHILD).limitToLast(1).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot snapshot, String s) {

                if(isPaused)
                {
                    mHandler.postAtTime(new Runnable() {
                        @Override
                        public void run() {
                            String text = (String) snapshot.child("text").getValue();
                            showNotification(text);
                        }
                    },5000);
                }

            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot snapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // Set default username is anonymous.
        mUsername = ANONYMOUS;
        //Initialize Auth
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        if (mUser == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mUsername = mUser.getDisplayName();
            if (mUser.getPhotoUrl() != null) {
                mPhotoUrl = mUser.getPhotoUrl().toString();
            }
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        // Initialize ProgressBar and RecyclerView.
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);


        mFirebaseAdapter = MessageUtil.getFirebaseAdapter(this,
                this,  /* MessageLoadListener */
                mLinearLayoutManager,
                mMessageRecyclerView,mImageClickListener);
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);

        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MSG_LENGTH_LIMIT)});
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mSendButton = (FloatingActionButton) findViewById(R.id.sendButton);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Send messages on click.
                mMessageRecyclerView.scrollToPosition(0);
                ChatMessage chatMessage = new
                        ChatMessage(mMessageEditText.getText().toString(),
                        mUsername,
                        mPhotoUrl);
                MessageUtil.send(chatMessage);
                mMessageEditText.setText("");
            }
        });



        mImageButton = (ImageButton) findViewById(R.id.shareImageButton);
        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });

        mLocationButton = (ImageButton) findViewById(R.id.locationButton);
        mLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMap();
            }
        });

        mCameraButton = (ImageButton) findViewById(R.id.cameraButton);

        mCameraButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                dispatchTakePhotoIntent();
            }
        });



        /**
         * Retrieve the Intent that was used to launch this activity.
         */
        Intent intent = getIntent();

        /**
         * Try to get the string parameter called 'param'. This parameter is set
         * in showNotification().
         */
        //String param = intent.getStringExtra("param");
        String param = getMessagetext(intent);

        if (param != null) {

            //Send the message to the mobile app
            ChatMessage chatMessage = new
                    ChatMessage(param,
                    mUsername,
                    mPhotoUrl);
            MessageUtil.send(chatMessage);
            mMessageEditText.setText("");
            //Toast.makeText(this,param,Toast.LENGTH_LONG).show();
        }

        ListView lv = (ListView) findViewById(R.id.lv);
        final ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(this, R.layout.listview_row_layout);
        lv.setAdapter(mAdapter);

        //final View rootView = findViewById(R.id.root_view);
        View rootView = getWindow().getDecorView().getRootView();
        final ImageView emojiButton = (ImageView) findViewById(R.id.emoji_btn);

        // Give the topmost view of your activity layout hierarchy. This will be used to measure soft keyboard height
        final EmojiconsPopup popup = new EmojiconsPopup(rootView, this);

        //Will automatically set size according to the soft keyboard size
        popup.setSizeForSoftKeyboard();

        //If the emoji popup is dismissed, change emojiButton to smiley icon
        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                changeEmojiKeyboardIcon(emojiButton, R.drawable.smiley);
            }
        });

        //If the text keyboard closes, also dismiss the emoji popup
        popup.setOnSoftKeyboardOpenCloseListener(new EmojiconsPopup.OnSoftKeyboardOpenCloseListener() {

            @Override
            public void onKeyboardOpen(int keyBoardHeight) {

            }

            @Override
            public void onKeyboardClose() {
                if(popup.isShowing())
                    popup.dismiss();
            }
        });

        //On emoji clicked, add it to edittext
        popup.setOnEmojiconClickedListener(new EmojiconGridView.OnEmojiconClickedListener() {

            @Override
            public void onEmojiconClicked(Emojicon emojicon) {
                if (mMessageEditText == null || emojicon == null) {
                    return;
                }

                int start = mMessageEditText.getSelectionStart();
                int end = mMessageEditText.getSelectionEnd();
                if (start < 0) {
                    mMessageEditText.append(emojicon.getEmoji());
                } else {
                    mMessageEditText.getText().replace(Math.min(start, end),
                            Math.max(start, end), emojicon.getEmoji(), 0,
                            emojicon.getEmoji().length());
                }
            }
        });

        //On backspace clicked, emulate the KEYCODE_DEL key event
        popup.setOnEmojiconBackspaceClickedListener(new EmojiconsPopup.OnEmojiconBackspaceClickedListener() {

            @Override
            public void onEmojiconBackspaceClicked(View v) {
                KeyEvent event = new KeyEvent(
                        0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                mMessageEditText.dispatchKeyEvent(event);
            }
        });

        // To toggle between text keyboard and emoji keyboard keyboard(Popup)
        emojiButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //If popup is not showing => emoji keyboard is not visible, we need to show it
                if(!popup.isShowing()){

                    //If keyboard is visible, simply show the emoji popup
                    if(popup.isKeyBoardOpen()){
                        popup.showAtBottom();
                        changeEmojiKeyboardIcon(emojiButton, R.drawable.ic_action_keyboard);
                    }

                    //else, open the text keyboard first and immediately after that show the emoji popup
                    else{
                        mMessageEditText.setFocusableInTouchMode(true);
                        mMessageEditText.requestFocus();
                        popup.showAtBottomPending();
                        final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.showSoftInput(mMessageEditText, InputMethodManager.SHOW_IMPLICIT);
                        changeEmojiKeyboardIcon(emojiButton, R.drawable.ic_action_keyboard);
                    }
                }

                //If popup is showing, simply dismiss it to show the undelying text keyboard
                else{
                    popup.dismiss();
                }
            }
        });



        //SETTING ONCLICK LISTENER FOR RECORDING BUTTON
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName+="/chathub_audio.mp3";
        mRecordButton = (ImageButton)findViewById(R.id.recordBtn);
        mRecordButton.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent){

                if(motionEvent.getAction()==MotionEvent.ACTION_DOWN)
                {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startRecording();
                            Toast.makeText(MainActivity.this,"Recording",Toast.LENGTH_SHORT).show();
                        }
                    });

                }
                else if(motionEvent.getAction()==MotionEvent.ACTION_UP)
                {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            stopRecording();
                            Toast.makeText(MainActivity.this,"Recording Done!",Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                return false;
            }
        });

        mMediaplayer = new MediaPlayer();
        mMediaplayer.setAudioStreamType(AudioManager.STREAM_MUSIC);


    }  //Oncreate ends



    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }

        mRecorder.start();
    }


    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Uri file = Uri.fromFile(new File(mFileName));
                upload(file);
            }
        });




    }



    private void upload(Uri file) {

        mProgressBar.setVisibility(View.VISIBLE);
        FirebaseStorage storageReference = FirebaseStorage.getInstance();
        StorageReference referent = storageReference.getReference().child("audio/"+file.getLastPathSegment());


        referent.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //Toast.makeText(MainActivity.this,"Download "+taskSnapshot.getDownloadUrl(),Toast.LENGTH_SHORT).show();
                mProgressBar.setVisibility(View.INVISIBLE);
                audioURL = String.valueOf(taskSnapshot.getDownloadUrl());

                //Log.i("onSuccess", "Download : " + taskSnapshot.getDownloadUrl() );
                String url = String.valueOf(taskSnapshot.getDownloadUrl());
                if(!url.isEmpty()){

                            ChatMessage chatMessage = new
                                    ChatMessage("click to play once!",
                                    mUsername,
                                    mPhotoUrl);
                            MessageUtil.send(chatMessage);
                            mMessageEditText.setText("");

                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("onFailure",e.toString());
                Toast.makeText(MainActivity.this,e.toString(),Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void fetchAudioUrlFromFirebase() {
        final FirebaseStorage storage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        StorageReference storageRef = storage.getReferenceFromUrl(audioURL);
        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(final Uri uri) {
                // Download url of file

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        final String url = uri.toString();
                        try {
                            mMediaplayer.setDataSource(url);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        // wait for media player to get prepare
                        mMediaplayer.setOnPreparedListener(MainActivity.this);
                        mMediaplayer.prepareAsync();

                    }
                });

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("TAG", e.getMessage());
                    }
                });

    }

    //This method is responsible to play the file from firebase database
    @Override
    public void onPrepared(final MediaPlayer player) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                player.start();
            }
        });

    }


    private void changeEmojiKeyboardIcon(ImageView iconToBeChanged, int drawableResourceId){
        iconToBeChanged.setImageResource(drawableResourceId);
    }

    //get the text from the wearable
    private String getMessagetext(Intent intent){

        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);

        if(remoteInput!=null)
        {
            return remoteInput.getCharSequence(EXTRA_VOICE_REPLY).toString();
        }
        return null;
    }


    private void showNotification(String param) {

        NotificationCompat.BigTextStyle bigStyle = new NotificationCompat.BigTextStyle();
        bigStyle.bigText(param);


        String replyLabel = getResources().getString(R.string.reply_text);
        String[] replyChoices = getResources().getStringArray(R.array.reply_choices);



        /**
         * The stack builder object will contain an artificial back stack for
         * the started activity. This ensures that navigating backward from the
         * MainActivity leads out of the application to the Home screen.
         */
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        /**
         * Adds the back stack for the Intent (but not the Intent itself)
         */
        stackBuilder.addParentStack(MainActivity.class);

        /**
         * Adds the Intent that starts the MainActivity to the top of the stack
         */
        //stackBuilder.addNextIntent(resultIntent);

        /**
         * Generate a PendingIntent from the TaskStackBuilder. The PendingIntent
         * essentially is a wrapper for resultIntent that will be fired when the
         * user taps on the notification in the status bar.
         */
        //PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                //PendingIntent.FLAG_UPDATE_CURRENT);
        //mBuilder.setContentIntent(resultPendingIntent);

        /**
         * Use the NotificationCompat to show the Notification that will extend to the android wear
         */
        RemoteInput remoteInput = new RemoteInput.Builder(EXTRA_VOICE_REPLY)
                                    .setLabel(replyLabel)
                                    .setChoices(replyChoices)
                                    .build();



        //Create an Intent to get the reply_action
        Intent replyIntent = new Intent(this,MainActivity.class);

        PendingIntent replyPendingIntent = PendingIntent.getActivity(this,0,replyIntent,PendingIntent.FLAG_UPDATE_CURRENT);


        replyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);


        stackBuilder.addNextIntent(replyIntent);


        //Create the reply action and add the remote input
        NotificationCompat.Action action1 = new NotificationCompat.Action.Builder(R.drawable.ic_reply_back_48px,
                                            getString(R.string.reply_text),replyPendingIntent)
                                            .addRemoteInput(remoteInput).build();



        //Create the reply action and add the remote input
        NotificationCompat.Action action2 = new NotificationCompat.Action.Builder(R.drawable.ic_cancel_48px,
                getString(R.string.cancel),NotificationActivity.getDismissIntent(1,this))
                .build();


        //Storing actions in a List Data Structure
        List<NotificationCompat.Action> actions = new ArrayList<>();
        actions.add(action1);
        actions.add(action2);


        Notification notification = new NotificationCompat.Builder(this)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSmallIcon(R.drawable.ic_read_message).setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_SOUND).setContentTitle("New Chathub Content!")
                .setContentText("Unread message")
                .setStyle(bigStyle)
                .extend(new android.support.v4.app.NotificationCompat.WearableExtender()
                        .addActions(actions)).build();

        replyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        notification.contentIntent = replyPendingIntent;


        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(this);

        mNotificationManager.notify(1,notification);


    }

    private void dispatchTakePhotoIntent() {

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(isAvailable(this,cameraIntent))
        {
         startActivityForResult(cameraIntent,REQUEST_TAKE_PHOTO);
        }

    }


    public static boolean isAvailable(Context ctx, Intent intent) {
        final PackageManager mgr = ctx.getPackageManager();
        List<ResolveInfo> list =
                mgr.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }


    private void deleteNode(){

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Query query = ref.child("messages").orderByChild("text").equalTo("click to play once!");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                    appleSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled", databaseError.toException());
            }
        });
    }



    private View.OnClickListener mImageClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            ImageView photoView =  (ImageView) v.findViewById(R.id.messageImageView);
            // Only show the larger view in dialog if there's a image for the message
            if(photoView.getDrawable()==null)
            {
                mProgressDialog = new ProgressDialog(MainActivity.this);
                mProgressDialog.setMessage("Streaming Voice Message -it will take few seconds!");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //code to do the HTTP request

                        fetchAudioUrlFromFirebase();
                        //Toast.makeText(MainActivity.this,"Playing",Toast.LENGTH_LONG).show();

                        mProgressDialog.show();

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //Do something after 100ms

                                deleteNode();
                                mProgressDialog.dismiss();
                            }
                        }, 3500);


                    }


                    /*
                    handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //Do something after 100ms

                                //deleteNode();

                            }
                        }, 5000);
                     */
                });

            }
            else if (photoView.getVisibility() == View.VISIBLE) {
                Bitmap bitmap = ((GlideBitmapDrawable) photoView.getDrawable()).getBitmap();
                showPhotoDialog(ImageDialogFragment.newInstance(bitmap));
            }
        }
    };

    void showPhotoDialog(DialogFragment dialogFragment) {

        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) { ft.remove(prev); }
        ft.addToBackStack(null);
        dialogFragment.show(ft, "dialog");
    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in.
        // TODO: Add code to check if user is signed in.
    }


    @Override
    public void onPause() {
        super.onPause();
        isPaused = true;
        if(isPaused)
        {
            Log.d("is paused is ","true");
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        isPaused = false;
        if(!isPaused)
        {
            Log.d("is paused is ","false");
        }
        LocationUtils.startLocationUpdates(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        boolean isGranted = (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED);
        if (isGranted && requestCode == LocationUtils.REQUEST_CODE) {
            LocationUtils.startLocationUpdates(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                mAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mUsername = ANONYMOUS;
                startActivity(new Intent(this, SignInActivity.class));
                return true;
            case R.id.preferences_menu:
                mSavedTheme = DesignUtils.getPreferredTheme(this);
                Intent i = new Intent(this, PreferencesActivity.class);
                startActivityForResult(i, REQUEST_PREFERENCES);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoadComplete() {
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);
    }

    private void pickImage() {
        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file browser
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened"
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        intent.setType("image/*");

        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: request=" + requestCode + ", result=" + resultCode);

        if (requestCode == REQUEST_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            // Process selected image here
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            if (data != null) {
                Uri uri = data.getData();
                Log.i(TAG, "Uri: " + uri.toString());

                // Resize if too big for messaging
                Bitmap bitmap = getBitmapForUri(uri);
                Bitmap resizedBitmap = scaleImage(bitmap);
                if (bitmap != resizedBitmap) {
                    uri = savePhotoImage(resizedBitmap);
                }

                createImageMessage(uri);
            } else {
                Log.e(TAG, "Cannot get image for uploading");
            }
        } else if (requestCode == REQUEST_PREFERENCES) {
            if (DesignUtils.getPreferredTheme(this) != mSavedTheme) {
                DesignUtils.applyColorfulTheme(this);
                this.recreate();
            }
        }
        else if (requestCode==REQUEST_TAKE_PHOTO && resultCode==Activity.RESULT_OK){
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                Bitmap imageBitmap = (Bitmap) extras.get("data");
               Uri savedImageUri = savePhotoImage(imageBitmap);
                createImageMessage(savedImageUri);
            }

        }
    }

    private void createImageMessage(Uri uri) {
        if (uri == null) {
            Log.e(TAG, "Could not create image message with null uri");
            return;
        }


        final StorageReference imageReference = MessageUtil.getImageStorageReference(mUser, uri);
        UploadTask uploadTask = imageReference.putFile(uri);

        // Register observers to listen for when task is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e(TAG, "Failed to upload image message");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                ChatMessage chatMessage = new
                        ChatMessage(mMessageEditText.getText().toString(),
                        mUsername,
                        mPhotoUrl, imageReference.toString());
                MessageUtil.send(chatMessage);
                mMessageEditText.setText("");
            }
        });
    }

    private Bitmap getBitmapForUri(Uri imageUri) {
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private Bitmap scaleImage(Bitmap bitmap) {
        int originalHeight = bitmap.getHeight();
        int originalWidth = bitmap.getWidth();
        double scaleFactor =  MAX_LINEAR_DIMENSION / (double)(originalHeight + originalWidth);

        // We only want to scale down images, not scale upwards
        if (scaleFactor < 1.0) {
            int targetWidth = (int) Math.round(originalWidth * scaleFactor);
            int targetHeight = (int) Math.round(originalHeight * scaleFactor);
            return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
        } else {
            return bitmap;
        }
    }

    private Uri savePhotoImage(Bitmap imageBitmap) {
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (photoFile == null) {
            Log.d(TAG, "Error creating media file");
            return null;
        }

        try {
            FileOutputStream fos = new FileOutputStream(photoFile);
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
        return Uri.fromFile(photoFile);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        String imageFileNamePrefix = "chathub-" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File imageFile = File.createTempFile(
                imageFileNamePrefix,    /* prefix */
                ".jpg",                 /* suffix */
                storageDir              /* directory */
        );
        return imageFile;
    }


    private void loadMap() {
        Loader<Bitmap> loader = getSupportLoaderManager().initLoader(0, null, new LoaderManager
                .LoaderCallbacks<Bitmap>() {
            @Override
            public Loader<Bitmap> onCreateLoader(final int id, final Bundle args) {
                return new MapLoader(MainActivity.this);
            }

            @Override
            public void onLoadFinished(final Loader<Bitmap> loader, final Bitmap result) {
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                mLocationButton.setEnabled(true);

                if (result == null) return;
                // Resize if too big for messaging
                Bitmap resizedBitmap = scaleImage(result);
                Uri uri = null;
                if (result != resizedBitmap) {
                    uri = savePhotoImage(resizedBitmap);
                } else {
                    uri = savePhotoImage(result);
                }
                createImageMessage(uri);

            }

            @Override
            public void onLoaderReset(final Loader<Bitmap> loader) {
            }

        });

        mProgressBar.setVisibility(ProgressBar.VISIBLE);
        mLocationButton.setEnabled(false);
        loader.forceLoad();
    }



}
