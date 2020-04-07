package com.chatelite.activities;

import android.Manifest;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.chatelite.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VideoCall extends AppCompatActivity
        implements Session.SessionListener, PublisherKit.PublisherListener {

    private static String API_Key = "46539322";
    private static String SESSION_ID = "2_MX40NjUzOTMyMn5-MTU4NDA1NzY0NjM4Mn5LaGNud2hBUUVjaUhlZXRpa0RFRWVOS1V-fg";
    private static String TOKEN = "T1==cGFydG5lcl9pZD00NjUzOTMyMiZzaWc9MTZlOTQyYjQ0ODdhOWU5N2FkYjNiNDEyYWExNDBlMjBlZTNiZDc0YjpzZXNzaW9uX2lkPTJfTVg0ME5qVXpPVE15TW41LU1UVTROREExTnpZME5qTTRNbjVMYUdOdWQyaEJVVVZqYVVobFpYUnBhMFJGUldWT1MxVi1mZyZjcmVhdGVfdGltZT0xNTg0MDU3NzU5Jm5vbmNlPTAuMTM4Mjc0NTg3MDA1Mjc3OTUmcm9sZT1wdWJsaXNoZXImZXhwaXJlX3RpbWU9MTU4NjY0OTc1OCZpbml0aWFsX2xheW91dF9jbGFzc19saXN0PQ==";
    private static final String LOG_TAG = VideoCall.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERM = 124;
    private ImageView closeVideoChatBtn;
    private DatabaseReference usersRef;
    private String userID = "";
    private FrameLayout mPublisherViewController;
    private FrameLayout mSubscriberViewController;
    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_call);

        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        closeVideoChatBtn = findViewById(R.id.close_video_chat_btn);
        closeVideoChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usersRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(userID).hasChild("Ringing")) {
                            usersRef.child(userID).child("Ringing").removeValue();

                            if (mPublisher != null) {
                                mPublisher.destroy();
                            }
                            if (mSubscriber != null) {
                                mSubscriber.destroy();
                            }

                            startActivity(new Intent(VideoCall.this, Main.class));
                            finish();
                        }
                        if (dataSnapshot.child(userID).hasChild("Ringing")) {
                            usersRef.child(userID).child("Calling").removeValue();

                            if (mPublisher != null) {
                                mPublisher.destroy();
                            }
                            if (mSubscriber != null) {
                                mSubscriber.destroy();
                            }

                            startActivity(new Intent(VideoCall.this, Main.class));
                            finish();
                        } else {
                            if (mPublisher != null) {
                                mPublisher.destroy();
                            }
                            if (mSubscriber != null) {
                                mSubscriber.destroy();
                            }

                            startActivity(new Intent(VideoCall.this, Main.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

        });

        requestPermission();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, VideoCall.this);

    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermission() {
        String[] perms = {Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};


        if (EasyPermissions.hasPermissions(this, perms)) {
            mPublisherViewController = findViewById(R.id.publisher_container);
            mSubscriberViewController = findViewById(R.id.subscriber_container);
            mSession = new Session.Builder(this, API_Key, SESSION_ID).build();
            mSession.setSessionListener(VideoCall.this);
            mSession.connect(TOKEN);
        } else {
            EasyPermissions.requestPermissions(this, "Hi this app needs the Mic and Camera permissions, Please allow", RC_VIDEO_APP_PERM, perms);

        }
    }


    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }

    @Override
    public void onConnected(Session session) {
        Log.i(LOG_TAG, "Session Connected");

        mPublisher = new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(VideoCall.this);
        mPublisherViewController.addView(mPublisher.getView());
        if (mPublisher.getView() instanceof GLSurfaceView) {
            ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);
        }
        mSession.publish(mPublisher);

    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(LOG_TAG, "Stream Disconnecteded");

    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Received");
        if (mSubscriber == null) {
            mSubscriber = new Subscriber.Builder(this, stream).build();
            mSession.subscribe(mSubscriber);
            mSubscriberViewController.addView(mSubscriber.getView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Dropped");
        if (mSubscriber != null) {
            mSubscriber = null;
            mSubscriberViewController.removeAllViews();
        }

    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.i(LOG_TAG, "Stream Error");

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
