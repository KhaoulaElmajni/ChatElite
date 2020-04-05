package com.chatelite.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chatelite.adapters.Message;
import com.chatelite.R;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class Discussion extends AppCompatActivity {

    boolean touched = true;
    private static final String LOG_TAG = "AudioRecordTest";
    private static String mFileName = null;
    private String currentUserID;
    private MediaRecorder mRecorder = null;
    public static boolean isDiscussionActivityRunning = false;
    private MediaPlayer mPlayer = null;
    String senderName = "";
    private String messageReceiverID, messageReceiverName, messageReceiverImage, messageSenderID, deviceToken;
    private MediaPlayer mp;
    private TextView userName, userLastSeen;
    private CircleImageView userImage;
    private Toolbar ChatToolbar;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private ImageButton SendMessageButton;
    private ImageView SendFilesButton;
    private EditText MessageInputText;
    private final List<com.chatelite.models.Message> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private Message messageAdapter;
    private RecyclerView userMessagesList;
    private String saveCurrentTime, saveCurrentDate;
    private String checker = "", myUri = "";
    private StorageTask uploadTask;
    private Uri fileUri;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.discussion);


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();

        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        messageReceiverImage = getIntent().getExtras().get("visit_user_image").toString();

        deviceToken = getIntent().getExtras().get("device_token").toString();


        InitializeControllers();


        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMessage();
            }
        });
        DisplayLastSeen();


        SendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence options[] = new CharSequence[]
                        {"Images",
                                "PDF Files",
                                "MS Word Files"
                        };
                AlertDialog.Builder builder = new AlertDialog.Builder(Discussion.this);
                builder.setTitle("Select File");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            checker = "image";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "Select image"), 438);
                        }
                        if (i == 1) {
                            checker = "pdf";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent, "Select PDF File"), 438);
                        }
                        if (i == 2) {
                            checker = "docx";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent, "Select MS word File"), 438);
                        }
                    }
                });
                builder.show();
            }
        });


        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/font6.ttf");

        RelativeLayout layout = findViewById(R.id.layout);
        ArrayList<View> clds = getAllChildren(layout);
        for (int i = 0; i < clds.size(); i += 1) {

            if (clds.get(i) instanceof TextView) {
                ((TextView) clds.get(i)).setTypeface(custom_font);
            }

            if (clds.get(i) instanceof Button) {
                ((Button) clds.get(i)).setTypeface(custom_font);
            }
        }


        ImageView record = findViewById(R.id.record);
        record.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (touched) {
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyyyyHHmmss", Locale.getDefault());
                            String currentDateAndTime = simpleDateFormat.format(new Date());

                            mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
                            mFileName += "/ChatElite/Media/Audios/" + currentDateAndTime + ".3gp";
                            startRecording();
                            touched = false;
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:

                        //Log.i("TAG", "moving: (" + x + ", " + y + ")");
                        break;
                    case MotionEvent.ACTION_UP:
                        touched = true;
                        stopRecording();

                        while (!new File(mFileName).exists()) ;

                        uploadFile("AUDIO", mFileName);


                        break;
                }

                return true;
            }
        });


        //TODO: To review later !

        RootRef.child("Message").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        com.chatelite.models.Message message = dataSnapshot.getValue(com.chatelite.models.Message.class);
                        stopPlaying();
                        mp = MediaPlayer.create(Discussion.this, R.raw.incoming_message);
                        mp.start();
                        messagesList.add(message);
                        messageAdapter.notifyDataSetChanged();
                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


        //TODO:
       /* RecordView recordView = findViewById(R.id.record_view);
        RecordButton recordButton = findViewById(R.id.record_button);

        //IMPORTANT
        recordButton.setRecordView(recordView);
*/

    }


    private void uploadFile(String fileType, String fileName) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

// Create a reference to "mountains.jpg"
        //StorageReference mountainsRef = storageRef.child(fileName);

// Create a reference to 'images/mountains.jpg'
        StorageReference mountainImagesRef = storageRef.child("CHATELITE" + "/AUDIOS/" + fileName.split("/")[fileName.split("/").length - 1]);

        InputStream stream = null;
        try {
            stream = new FileInputStream(new File(fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        uploadTask = mountainImagesRef.putStream(stream);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(Discussion.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(Discussion.this, "Uploaded", Toast.LENGTH_SHORT).show();

                SendAudio(taskSnapshot.getUploadSessionUri().toString());


            }
        });
    }


    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        Toast.makeText(Discussion.this, "Stopped !", Toast.LENGTH_SHORT).show();
    }

    private void startRecording() {
        Toast.makeText(Discussion.this, "Started !", Toast.LENGTH_SHORT).show();
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        /*
        String manufacturer = Build.MANUFACTURER;
        if (manufacturer.toLowerCase().contains("samsung")) {
            mRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
        } else {
            mRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
        }*/
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }


    private ArrayList<View> getAllChildren(View v) {

        if (!(v instanceof ViewGroup)) {
            ArrayList<View> viewArrayList = new ArrayList<>();
            viewArrayList.add(v);
            return viewArrayList;
        }
        ArrayList<View> result = new ArrayList<>();
        ViewGroup viewGroup = (ViewGroup) v;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            ArrayList<View> viewArrayList = new ArrayList<>();
            viewArrayList.add(v);
            viewArrayList.addAll(getAllChildren(child));
            result.addAll(viewArrayList);
        }
        return result;
    }


    private void InitializeControllers() {

        ChatToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(ChatToolbar);

        ActionBar actionBar = getSupportActionBar();
        //actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar_old, null);
        actionBar.setCustomView(actionBarView);


        getSupportActionBar().setDisplayShowTitleEnabled(false);


        userImage = findViewById(R.id.custom_profile_image);
        userName = findViewById(R.id.custom_profile_name);
        userLastSeen = findViewById(R.id.custom_user_last_seen);
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/font6.ttf");
        userName.setTypeface(custom_font);
        userLastSeen.setTypeface(custom_font);
        SendMessageButton = findViewById(R.id.send_message_btn);
        SendFilesButton = findViewById(R.id.send_files_btn);

        MessageInputText = findViewById(R.id.input_message);


        MessageInputText.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {


                        if (s.length() != 0) {
                            /*
                            final String messageSenderRef = "Users/" + messageSenderID + "/userState" ;

                            //DatabaseReference userMessageKeyRef = RootRef.child("States").child(messageSenderID).child(messageReceiverID).push();
                            //final String messagePushID = userMessageKeyRef.getKey();
                            Map messageTextBody = new HashMap();
                            messageTextBody.put("state", "Typing");
                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef, messageTextBody);

                            RootRef.updateChildren(messageBodyDetails);
*/


                            RootRef.child("Users").child(messageSenderID).child("userState").child("state").setValue("Typing");


                        }


                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    private Timer timer = new Timer();
                    private final long DELAY = 500; // milliseconds

                    @Override
                    public void afterTextChanged(final Editable s) {
                        timer.cancel();
                        timer = new Timer();
                        timer.schedule(
                                new TimerTask() {
                                    @Override
                                    public void run() {


                                        /*final String messageSenderRef = "Users/" + messageSenderID + "/userState" ;

                                        //DatabaseReference userMessageKeyRef = RootRef.child("States").child(messageSenderID).child(messageReceiverID).push();
                                        //final String messagePushID = userMessageKeyRef.getKey();
                                        Map messageTextBody = new HashMap();
                                        messageTextBody.put("state", "Online");
                                        Map messageBodyDetails = new HashMap();
                                        messageBodyDetails.replace(messageSenderRef, messageTextBody);

                                        RootRef.updateChildren(messageBodyDetails);*/

                                        RootRef.child("Users").child(messageSenderID).child("userState").child("state").setValue("Online");


                                    }
                                },
                                DELAY
                        );
                    }
                }
        );

        messageAdapter = new Message(this, messagesList);

        userMessagesList = findViewById(R.id.private_messages_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);

        loadingBar = new ProgressDialog(this);

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MM/dd/yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());


        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == 438 && resultCode == RESULT_OK && data != null && data.getData() != null) {

            loadingBar.setTitle("Sending File");
            loadingBar.setMessage("Please wait, while your file is sending... ");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            fileUri = data.getData();
            if (!checker.equals("image")) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");

                final String messageSenderRef = "Message/" + messageSenderID + "/" + messageReceiverID;
                final String messageReceiverRef = "Message/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = RootRef.child("Message")
                        .child(messageSenderID).child(messageReceiverID).push();
                final String messagePushID = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushID + "." + checker);

                filePath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Map messageTextBody = new HashMap();
                            //TODO: messageTextBody.put("message", task.getResult().getDownloadUrl().toString());
                            messageTextBody.put("message", task.getResult().toString());
                            messageTextBody.put("name", fileUri.getLastPathSegment());
                            messageTextBody.put("type", checker);
                            messageTextBody.put("from", messageSenderID);
                            messageTextBody.put("to", messageReceiverID);
                            messageTextBody.put("messageID", messagePushID);
                            messageTextBody.put("time", saveCurrentTime);
                            messageTextBody.put("date", saveCurrentDate);


                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

                            RootRef.updateChildren(messageBodyDetails);
                            loadingBar.dismiss();

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingBar.dismiss();
                        Toast.makeText(Discussion.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double p = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        loadingBar.setMessage((int) p + " % Uploading...");
                    }
                });
            } else if (checker.equals("image")) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

                final String messageSenderRef = "Message/" + messageSenderID + "/" + messageReceiverID;
                final String messageReceiverRef = "Message/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = RootRef.child("Message")
                        .child(messageSenderID).child(messageReceiverID).push();
                final String messagePushID = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushID + ".jpg");
                uploadTask = filePath.putFile(fileUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            stopPlaying();
                            mp = MediaPlayer.create(Discussion.this, R.raw.outgoing_message);
                            mp.start();
                            Uri downloadUri = task.getResult();
                            myUri = downloadUri.toString();

                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message", myUri);
                            messageTextBody.put("name", fileUri.getLastPathSegment());
                            messageTextBody.put("type", checker);
                            messageTextBody.put("from", messageSenderID);
                            messageTextBody.put("to", messageReceiverID);
                            messageTextBody.put("messageID", messagePushID);
                            messageTextBody.put("time", saveCurrentTime);
                            messageTextBody.put("date", saveCurrentDate);


                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

                            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {
                                        loadingBar.dismiss();
                                        //Toast.makeText(Discussion.this, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();

                                    } else {
                                        loadingBar.dismiss();
                                        Toast.makeText(Discussion.this, "ERROR:", Toast.LENGTH_SHORT).show();

                                    }
                                    MessageInputText.setText("");
                                }
                            });
                        }
                    }
                });

            } else {
                loadingBar.dismiss();
                Toast.makeText(this, "ERROR : Nothing Selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void DisplayLastSeen() {
        RootRef.child("Users").child(messageReceiverID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child("userState").hasChild("state")) {
                            String state = dataSnapshot.child("userState").child("state").getValue().toString();
                            String date = dataSnapshot.child("userState").child("date").getValue().toString();
                            String time = dataSnapshot.child("userState").child("time").getValue().toString();
                            if (state.equals("Online")) {
                                userLastSeen.setText("Online");
                            } else if (state.equals("Offline")) {
                                Calendar calendar = Calendar.getInstance();
                                SimpleDateFormat currentDate = new SimpleDateFormat("dd/MM/yyyy");
                                String current_Date = currentDate.format(calendar.getTime());

                                calendar.add(Calendar.DATE, -1);
                                SimpleDateFormat yesterdayDate = new SimpleDateFormat("dd/MM/yyyy");
                                String yesterday_Date = yesterdayDate.format(calendar.getTime());


                                if (current_Date.equals(date)) {
                                    date = "Today";
                                } else if (yesterday_Date.equals(date)) {
                                    date = "Yesterday";
                                }

                                userLastSeen.setText(date + " at " + time);
                            }
                        } else {
                            userLastSeen.setText("Offline");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void stopPlaying() {
        if (mp != null) {
            mp.stop();
            mp.release();
            mp = null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        isDiscussionActivityRunning = true;
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.d("ChatElite", "null");
            SendUserToLoginActivity();

        } else {
            Log.d("ChatElite", "not null");
            UpdateUserStatus("Online");
            VerifyUserExistance();
        }
        RootRef.child("Message").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        /** Message messages = dataSnapshot.getValue(Message.class);
                         stopPlaying();
                         mp = MediaPlayer.create(Discussion.this, R.raw.incoming_message);
                         mp.start();
                         messagesList.add(messages);
                         messageAdapter.notifyDataSetChanged();
                         userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                         **/
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void SendMessage() {
        final SQLiteDatabase chatEliteDB = openOrCreateDatabase("ChatEliteDB", MODE_PRIVATE, null);
        final String messageText = MessageInputText.getText().toString();

        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "First write your message...", Toast.LENGTH_SHORT).show();
        } else {
            String messageSenderRef = "Message/" + messageSenderID + "/" + messageReceiverID;
            String messageReceiverRef = "Message/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference userMessageKeyRef = RootRef.child("Message")
                    .child(messageSenderID).child(messageReceiverID).push();
            String messagePushID = userMessageKeyRef.getKey();
            Map<String, String> messageTextBody = new HashMap<>();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderID);
            messageTextBody.put("to", messageReceiverID);
            messageTextBody.put("messageID", messagePushID);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);
            messageTextBody.put("MessageState", "SENT");


            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        RootRef.child("Users").child(currentUserID).child("name").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                senderName = dataSnapshot.getValue(String.class);

                                try {
                                    Jsoup.connect("https://fcm.googleapis.com/fcm/send")
                                            .userAgent("Mozilla")
                                            .header("Content-type", "application/json")
                                            .header("Authorization", "key=AIzaSyDKXlWHYXZJqeezKjXtrQM43x8AQd9Zgl4")
                                            .requestBody("{\"notification\":{\"title\":\"" + senderName + "\",\"body\":\"" + messageText + "\"},\"to\" : \"" + deviceToken + "\"}")
                                            .post();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }


                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        ContentValues contentValues = new ContentValues();
                        //contentValues.put("MessageId", messageText);
                        contentValues.put("MessageContent", messageText);
                        contentValues.put("MessageState", "SENT");
                        contentValues.put("MessageType", "TEXT");
                        contentValues.put("Sender", messageSenderID);
                        contentValues.put("Receiver", messageReceiverID);
                        contentValues.put("SendingDate", saveCurrentDate);
                        contentValues.put("SendingTime", saveCurrentTime);
                        chatEliteDB.insert("Message", null, contentValues);


                    } else {
                        Toast.makeText(Discussion.this, "ERROR:", Toast.LENGTH_SHORT).show();

                    }
                    MessageInputText.setText("");
                }
            });
        }
    }


    private void SendAudio(String audioUrl) {
        final SQLiteDatabase chatEliteDB = openOrCreateDatabase("ChatEliteDB", MODE_PRIVATE, null);
        final String messageText = MessageInputText.getText().toString();


        String messageSenderRef = "Message/" + messageSenderID + "/" + messageReceiverID;
        String messageReceiverRef = "Message/" + messageReceiverID + "/" + messageSenderID;

        DatabaseReference userMessageKeyRef = RootRef.child("Message")
                .child(messageSenderID).child(messageReceiverID).push();
        String messagePushID = userMessageKeyRef.getKey();
        Map<String, String> messageTextBody = new HashMap<>();
        messageTextBody.put("message", audioUrl);
        messageTextBody.put("type", "text");
        messageTextBody.put("from", messageSenderID);
        messageTextBody.put("to", messageReceiverID);
        messageTextBody.put("messageID", messagePushID);
        messageTextBody.put("time", saveCurrentTime);
        messageTextBody.put("date", saveCurrentDate);
        messageTextBody.put("MessageState", "SENT");


        Map messageBodyDetails = new HashMap();
        messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
        messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

        RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    Toast.makeText(Discussion.this, "Audio Sent Successfully", Toast.LENGTH_SHORT).show();

                    try {
                        Jsoup.connect("https://fcm.googleapis.com/fcm/send")
                                .userAgent("Mozilla")
                                .header("Content-type", "application/json")
                                .header("Authorization", "key=AIzaSyDKXlWHYXZJqeezKjXtrQM43x8AQd9Zgl4")
                                .requestBody("{\"notification\":{\"title\":\"" + "Full Name" + "\",\"body\":\"" + audioUrl + "\"},\"to\" : \"" + deviceToken + "\"}")
                                .post();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    //Since it has been sent successfully :

                    ContentValues contentValues = new ContentValues();
                    //contentValues.put("MessageId", messageText);
                    contentValues.put("MessageContent", audioUrl);
                    contentValues.put("MessageState", "SENT");
                    contentValues.put("MessageType", "TEXT");
                    contentValues.put("Sender", messageSenderID);
                    contentValues.put("Receiver", messageReceiverID);
                    contentValues.put("SendingDate", saveCurrentDate);
                    contentValues.put("SendingTime", saveCurrentTime);
                    chatEliteDB.insert("Message", null, contentValues);


                } else {
                    Toast.makeText(Discussion.this, "ERROR:", Toast.LENGTH_SHORT).show();

                }

            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.discussion_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.video_call) {
            Intent phoneLoginIntent = new Intent(Discussion.this, VideoCall.class);
            startActivity(phoneLoginIntent);
            return true;
        }

        if (id == R.id.voice_call) {


            try {
                Jsoup.connect("https://fcm.googleapis.com/fcm/send")
                        .userAgent("Mozilla")
                        .header("Content-type", "application/json")
                        .header("Authorization", "key=AIzaSyDKXlWHYXZJqeezKjXtrQM43x8AQd9Zgl4")
                        .requestBody("{\"data\":{\"title\":\"" + "Full Name" + "\",\"Type\":\"" + "Voice" + "\"},\"to\" : \"" + deviceToken + "\"}")
                        .post();
            } catch (IOException e) {
                e.printStackTrace();
            }


            //todo:
            Intent phoneLoginIntent = new Intent(Discussion.this, VoiceCall.class);
            phoneLoginIntent.putExtra("recipientId", deviceToken);
            startActivity(phoneLoginIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStop() {
        super.onStop();
        isDiscussionActivityRunning = false;

        if (!Main.isMainActivityRunning) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                UpdateUserStatus("Offline");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDiscussionActivityRunning = false;

        if (!Main.isMainActivityRunning) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                UpdateUserStatus("Offline");
            }
        }

    }

    private void VerifyUserExistance() {
        String currentUserID = mAuth.getCurrentUser().getUid();
        RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ((dataSnapshot.child("name").exists())) {
                    //TODO:
                    //Toast.makeText(Main.this, "Welcome", Toast.LENGTH_SHORT).show();
                } else {
                    SendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }


        });
    }

    private void SendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(getBaseContext(), Settings.class);
        startActivity(settingsIntent);
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(getBaseContext(), LoginByEmail.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void UpdateUserStatus(String state) {
        String saveCurrentTime, saveCurrentDate;
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("dd/MM/yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());


        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("state", state);

        currentUserID = mAuth.getCurrentUser().getUid();

        RootRef.child("Users").child(currentUserID).child("userState")
                .updateChildren(onlineStateMap);
    }

}
