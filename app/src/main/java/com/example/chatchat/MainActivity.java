package com.example.chatchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {


    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessorAsadpter myTabsAccessorAsadpter;
private DatabaseReference RootRef ;
    private FirebaseUser currentUser;
private FirebaseAuth mAuth ;

private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        RootRef = FirebaseDatabase.getInstance().getReference();

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("ChatElite");
        myViewPager=(ViewPager)findViewById(R.id.main_tabs_pager);
        myTabsAccessorAsadpter =new TabsAccessorAsadpter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessorAsadpter);
        myTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser==null){
            SendUserToLoginActivity();
        }
        else {
            UpdateUserStatus("online");

            VerifyUserExistance();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();

        if (currentUser!=null){
            UpdateUserStatus("offline");
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (currentUser!=null){
            UpdateUserStatus("offline");
        }

    }

    private void VerifyUserExistance() {
        String currentUserID = mAuth.getCurrentUser().getUid();
        RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ((dataSnapshot.child("name").exists())){
                    Toast.makeText(MainActivity.this,"Welcome",Toast.LENGTH_SHORT).show();
                }
                else {
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
        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       super.onOptionsItemSelected(item);
       if (item.getItemId() == R.id.main_logout_option){
           mAuth.signOut();
           SendUserToLoginActivity();
       }
        if (item.getItemId() == R.id.main_settings_option){
            SendUserToSettingsActivity();
        }
        if (item.getItemId() == R.id.main_find_friends_option){
            SendUserToFindFriendsActivity();
        }
        if (item.getItemId() == R.id.main_create_group_option){
            RequestNewGroup();
        }
        return true;
    }

    private void RequestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        builder.setTitle("Enter Group Name :");
        final EditText groupNameField= new EditText(MainActivity.this);
        groupNameField.setHint("EX : ChatElite");
        builder.setView(groupNameField);
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String groupName = groupNameField.getText().toString();
                if(TextUtils.isEmpty(groupName)){
                    Toast.makeText(MainActivity.this, "Please write a name for your group...", Toast.LENGTH_SHORT).show();
                }
                else {
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
        builder.show();
    }

    private void CreateNewGroup(final String groupName) {
        RootRef.child("Groups").child(groupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(MainActivity.this, groupName+" Group Is Created Successfully...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent =new Intent(MainActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
    private void SendUserToSettingsActivity() {
        Intent settingsIntent =new Intent(MainActivity.this,SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private void SendUserToFindFriendsActivity() {
        Intent findFriendsIntent =new Intent(MainActivity.this,FindFriendsActivity.class);
        startActivity(findFriendsIntent);

    }


    private  void UpdateUserStatus(String state){
        String saveCurrentTime , saveCurrentDate;
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate =new SimpleDateFormat("MMM dd,yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());


        SimpleDateFormat currentTime =new SimpleDateFormat("hh:mm: a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String,Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time",saveCurrentTime);
        onlineStateMap.put("date",saveCurrentDate);
        onlineStateMap.put("state",state);

        currentUserID = mAuth.getCurrentUser().getUid();

        RootRef.child("Users").child(currentUserID).child("userState")
                .updateChildren(onlineStateMap);
    }



}
