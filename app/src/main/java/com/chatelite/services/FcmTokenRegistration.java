package com.chatelite.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;


public class FcmTokenRegistration extends IntentService {

    public FcmTokenRegistration() {
        super("FcmTokenRegistration");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {

                            Log.d("Firebase getInstanceId failed ", task.getException().toString());
                            return;
                        }
                        String token = task.getResult().getToken();
                        final SharedPreferences preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                        preferences.edit().putString("FcmToken", token).apply();
                        String phone = preferences.getString("PhoneNumber", "Undefined");
                        if (!phone.equals("Undefined")) {
                        }
                        Log.d("MY-FCM", token);
                    }
                });
    }


}
