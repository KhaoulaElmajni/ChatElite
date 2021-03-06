package com.chatelite.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import com.chatelite.R;
import com.chatelite.adapters.TabsAccessor;
import com.chatelite.models.Group;
import com.chatelite.models.Member;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class Main extends AppCompatActivity {

    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessor myTabsAccessorAsadpter;
    private DatabaseReference RootRef;
    public static boolean isMainActivityRunning;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private StorageTask uploadTask;
    private String currentUserID;
    private String createdGroupPhoto;
    private ImageView theGroupPhoto = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);

        loadingBar = new ProgressDialog(this);
        ActivityCompat.requestPermissions(Main.this,
                new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.MODIFY_AUDIO_SETTINGS,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.INTERNET

                },
                1);


        Intent iin = getIntent();
        Bundle b = iin.getExtras();

        if (b != null) {
            String j = (String) b.get("Why");
            if (j.equals("Call")) {
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                r.play();
            }

        }


        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ChatElite/Media/";
        File file = new File(rootPath);
        if (!file.exists()) {
            file.mkdirs();
        }

        rootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ChatElite/Media/Images";
        file = new File(rootPath);
        if (!file.exists()) {
            file.mkdirs();
        }


        rootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ChatElite/Media/Documents";
        file = new File(rootPath);
        if (!file.exists()) {
            file.mkdirs();
        }


        rootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ChatElite/Media/Audios";
        file = new File(rootPath);
        if (!file.exists()) {
            file.mkdirs();
        }


        rootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ChatElite/Media/Videos";
        file = new File(rootPath);
        if (!file.exists()) {
            file.mkdirs();
        }


        //startActivity(new Intent(Main.this, VideoCall.class));


        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();
        mToolbar =  findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("ChatElite");
        myViewPager = findViewById(R.id.main_tabs_pager);
        myTabsAccessorAsadpter = new TabsAccessor(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessorAsadpter);
        myTabLayout =  findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);

        Toolbar toolbar = mToolbar;
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View view = toolbar.getChildAt(i);
            if (view instanceof TextView) {
                TextView tv = (TextView) view;
                Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/Bariol_Regular.otf");
                //if(tv.getText().equals(toolbar.getTitle())){
                tv.setTypeface(custom_font);
                // break;
                //}
            }
        }


        ViewGroup vg = (ViewGroup) myTabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();
        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildsCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildsCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/Bariol_Regular.otf");
                    ((TextView) tabViewChild).setTypeface(custom_font);
                }
            }
        }


        /*
        RelativeLayout StartLayout = findViewById(R.id.Layout);
         ArrayList<View> clds = getAllChildren(StartLayout);
         for (int i = 0; i < clds.size(); i += 1) {

         if (clds.get(i) instanceof TextView) {
         ((TextView) clds.get(i)).setTypeface(custom_font);
         }

         if (clds.get(i) instanceof Button) {
         ((Button) clds.get(i)).setTypeface(custom_font);
         }
         }*/


        final SQLiteDatabase chatEliteDB = openOrCreateDatabase("ChatEliteDB", MODE_PRIVATE, null);

        chatEliteDB.execSQL("CREATE TABLE IF NOT EXISTS Messages\n" +
                "(\n" +
                "    MessageId      INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "    MessageContent TEXT,\n" +
                "    MessageState   TEXT,\n" +
                "    MessageType    TEXT,\n" +
                "    Sender         TEXT,\n" +
                "    Receiver       TEXT,\n" +
                "    SendingDate    TEXT,\n" +
                "    SendingTime    TEXT\n" +
                "\n" +
                ");");


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

    @Override
    protected void onStart() {
        super.onStart();
        isMainActivityRunning = true;
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.d("ChatElite", "null");
            SendUserToLoginActivity();

        } else {
            Log.d("ChatElite", "not null");
            UpdateUserStatus("Online");
            VerifyUserExistance();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        isMainActivityRunning = false;
        if (!Discussion.isDiscussionActivityRunning) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                UpdateUserStatus("Offline");
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        isMainActivityRunning = false;
        if (!Discussion.isDiscussionActivityRunning) {
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.main_logout_option) {
            UpdateUserStatus("Offline");
            mAuth.signOut();
            SendUserToLoginActivity();
        }
        if (item.getItemId() == R.id.main_settings_option) {
            SendUserToSettingsActivity();
        }
        if (item.getItemId() == R.id.main_find_friends_option) {
            SendUserToFindFriendsActivity();
        }
        if (item.getItemId() == R.id.main_create_group_option) {
            RequestNewGroup();
        }
        return true;
    }

    private void RequestNewGroup() {


        final Dialog dialog = new Dialog(Main.this);
        dialog.setContentView(R.layout.new_group);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        EditText name = dialog.findViewById(R.id.groupName);
        Button create = dialog.findViewById(R.id.createGroup);
        ImageView groupPhoto = dialog.findViewById(R.id.groupPhoto);
        theGroupPhoto = groupPhoto;
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/Bariol_Regular.otf");
        name.setTypeface(custom_font);
        create.setTypeface(custom_font);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String groupName = name.getText().toString();
                if (TextUtils.isEmpty(groupName)) {
                    Toast.makeText(Main.this, "Please write a name for your group...", Toast.LENGTH_SHORT).show();
                } else {
                    CreateNewGroup(groupName, createdGroupPhoto);
                }


                dialog.dismiss();


            }
        });

        groupPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(Main.this);


            }
        });


        dialog.show();







/*
        AlertDialog.Builder builder = new AlertDialog.Builder(Main.this, R.style.AlertDialog);
        builder.setTitle("Enter Group Name :");
        final EditText groupNameField = new EditText(Main.this);
        groupNameField.setHint("EX : ChatElite");
        builder.setView(groupNameField);
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String groupName = groupNameField.getText().toString();
                if (TextUtils.isEmpty(groupName)) {
                    Toast.makeText(Main.this, "Please write a name for your group...", Toast.LENGTH_SHORT).show();
                } else {
                    CreateNewGroup(groupName);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.show();*/
    }

    private void CreateNewGroup(final String groupName, final String photo) {

        Group group = new Group();
        group.setName(groupName);
        group.setPhoto(photo);
        group.setAdmin(mAuth.getCurrentUser().getUid());
        DatabaseReference newRef = RootRef.child("Groups").push();
        newRef.setValue(group).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(Main.this, groupName + " Group Is Created Successfully...", Toast.LENGTH_SHORT).show();
                }
            }
        });


        Member member = new Member();
        member.setId(mAuth.getCurrentUser().getUid());
        DatabaseReference anotherNewRef = RootRef.child("Groups").child(newRef.getKey()).child("members").push();
        anotherNewRef.setValue(member).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(Main.this, groupName + " Member added Successfully...", Toast.LENGTH_SHORT).show();
                }
            }
        });





    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(Main.this, LoginByEmail.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void SendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(Main.this, Settings.class);
        startActivity(settingsIntent);
    }

    private void SendUserToFindFriendsActivity() {
        Intent findFriendsIntent = new Intent(Main.this, FindFriends.class);
        startActivity(findFriendsIntent);

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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(Main.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                loadingBar.setTitle("Updating Profile Picture");
                loadingBar.setMessage("Please wait, while your profile image is updating... ");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri = result.getUri();

                //StorageReference filePath = UserProfileImagesRef.child(currentUserID + ".jpg");


                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();

                // Create a reference to "mountains.jpg"
                //StorageReference mountainsRef = storageRef.child(fileName);

                // Create a reference to 'images/mountains.jpg'
                StorageReference mountainImagesRef = storageRef.child("CHATELITE" + "/PROFILES/" + resultUri.toString().split("/")[resultUri.toString().split("/").length - 1]);

                InputStream stream = null;
                try {
                    stream = new FileInputStream(new File(resultUri.getPath()));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                uploadTask = mountainImagesRef.putStream(stream);

                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(Main.this, "Failed", Toast.LENGTH_SHORT).show();
                        String message = exception.toString();
                        Toast.makeText(Main.this, "ERROR: " + message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(Main.this, "Uploaded", Toast.LENGTH_SHORT).show();


                        mountainImagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                createdGroupPhoto = uri.toString();
                                Picasso.get().load(createdGroupPhoto).into(theGroupPhoto);
                                loadingBar.dismiss();
                               /* RootRef.child("Groups").child(currentUserID).child("image").child("photo").setValue(uri.toString())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {

                                                    Toast.makeText(Main.this, "Image saved in Database Successfully...", Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();
                                                } else {
                                                    String message = task.getException().toString();
                                                    Toast.makeText(Main.this, "ERROR : " + message, Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Toast.makeText(Settings.this, "Image saved in Database Successfully...", Toast.LENGTH_SHORT).show();
                                    }
                                });

*/
                            }
                        });

                    }
                });


            }

        }


    }


}
