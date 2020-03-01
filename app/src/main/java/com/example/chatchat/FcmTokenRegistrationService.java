package com.example.chatchat;

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


import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.HashMap;

/*


2020-01-20 02:41:06.570 12432-12432/com.chatelite D/MY-FCM: fO8EhvJgghI:APA91bEwIfX2ieM3h6V3v5H88d7PdA8q-I4OW4pE8rxz97ByC_C32w4EWIGZ1_ye8CH_LsOoBFYykG8kueNooGUPTz2ojnyztoaXx928rMs89lNVsnbJBPB4SH1WfwgjqbZ0KAo-mpQz


 */


public class FcmTokenRegistrationService extends IntentService {

    public FcmTokenRegistrationService() {
        super("FcmTokenRegistrationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {

                            Logg.d("Firebase getInstanceId failed " + task.getException());
                            return;
                        }

                        // Get new Instance ID token
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
