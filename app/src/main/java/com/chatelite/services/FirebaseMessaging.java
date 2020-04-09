package com.chatelite.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.chatelite.R;
import com.chatelite.activities.Main;
import com.chatelite.activities.VoiceCall;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


import java.util.Objects;

public class FirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {

        Handler handler = new Handler(getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {
                if (remoteMessage.getData().size() > 0) {
                    Log.d("FCM", remoteMessage.getData().toString());


                    if (remoteMessage.getData().containsKey("Type")){

                        if (remoteMessage.getData().get("Type").equals("Voice")) {

                            Intent dialogIntent = new Intent(getApplicationContext(), VoiceCall.class);
                            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            dialogIntent.putExtra("id", remoteMessage.getData().get("to"));
                            startActivity(dialogIntent);
                        }
                }

                    if (remoteMessage.getData().containsKey("messageFrom")) {

                        DatabaseReference UsersRef = FirebaseDatabase.getInstance().getReference().child("Message")
                                .child(remoteMessage.getData().get("messageFrom")).child(remoteMessage.getData().get("messageTo"))
                                .child(remoteMessage.getData().get("messageId")).child("MessageState");
                        UsersRef.setValue("DELIVERED").addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("FCM", "SUCCEEDED");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("FCM", "FAILED");
                            }
                        });


                        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Message")
                                .child(remoteMessage.getData().get("messageTo")).child(remoteMessage.getData().get("messageFrom"))
                                .child(remoteMessage.getData().get("messageId")).child("MessageState");
                        usersRef.setValue("DELIVERED").addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("FCM", "SUCCEEDED");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("FCM", "FAILED");
                            }
                        });
                        Log.d("FCM", "PASSED !");

                    }


                }

                if (remoteMessage.getNotification() != null) {
                    int requestID = (int) System.currentTimeMillis();
                    Intent notificationIntent = new Intent(getApplicationContext(), Main.class);
                    PendingIntent contentIntent = PendingIntent.getActivity(FirebaseMessaging.this, requestID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    NotificationManager notificationManager;
                    NotificationCompat.Builder builder;
                    NotificationChannel channel;
                    notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    CharSequence name = "a";
                    String description = "b";
                    int importance = NotificationManager.IMPORTANCE_DEFAULT;
                    channel = new NotificationChannel("1", name, importance);
                    channel.setDescription(description);
                    builder = new NotificationCompat.Builder(FirebaseMessaging.this, channel.getId())
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(Objects.requireNonNull(remoteMessage.getNotification()).getTitle())
                            .setContentText(Objects.requireNonNull(remoteMessage.getNotification()).getBody());
                    notificationManager.createNotificationChannel(channel);
                    builder.setContentIntent(contentIntent);
                    notificationManager.notify(1, builder.build());
                }

/**

 NotifyMe notifyMe = new NotifyMe.Builder(getApplicationContext())
 .title(remoteMessage.getNotification().getTitle())
 .content(remoteMessage.getNotification().getBody())
 .color(255,0,0,255)
 .led_color(255,255,255,255)
 .time(new Date())
 .key("test")
 .addAction(new Intent(),"Dismiss",true,false)
 .addAction(new Intent(),"Dismiss",true,false)
 .addAction(new Intent(),"Dismiss",true,false)
 .large_icon(R.mipmap.ic_launcher)
 .rrule("FREQ=MINUTELY;INTERVAL=5;COUNT=2")
 .build();
 **/


            }
        });


    }

    @Override
    public void onNewToken(String registrationToken) {


        Log.d("Firebase #onNewToken registrationToken=", registrationToken);

        startService(new Intent(this, FcmTokenRegistration.class));
    }
}
