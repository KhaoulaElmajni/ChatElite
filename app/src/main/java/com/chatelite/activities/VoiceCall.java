package com.chatelite.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.chatelite.R;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.calling.CallListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class VoiceCall extends AppCompatActivity {

    private static final String APP_KEY = "3bc70000-95f5-43a1-98d4-d534dbf16118";
    private static final String APP_SECRET = "m6WqWVPqNU+MdIQFLbtB5Q==";
    private static final String ENVIRONMENT = "clientapi.sinch.com";

    private Call call;
    private TextView callState;
    private SinchClient sinchClient;
    private Button button;
    private String callerId;
    private String recipientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
        setContentView(R.layout.voice_call);
        final SharedPreferences preferences = this.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        Intent intent = getIntent();
        callerId = preferences.getString("FcmToken", "me");
        recipientId = intent.getStringExtra("recipientId");
        sinchClient = Sinch.getSinchClientBuilder()
                .context(this)
                .userId(callerId)
                .applicationKey(APP_KEY)
                .applicationSecret(APP_SECRET)
                .environmentHost(ENVIRONMENT)
                .build();

        sinchClient.setSupportCalling(true);
        sinchClient.startListeningOnActiveConnection();
        sinchClient.start();
        sinchClient.getCallClient().addCallClientListener(new SinchCallClientListener());


        button = findViewById(R.id.button);
        callState = findViewById(R.id.callState);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (call == null) {
                    call = sinchClient.getCallClient().callUser(recipientId);
                    call.addCallListener(new SinchCallListener());
                    button.setText("Hang Up");
                } else {
                    call.hangup();
                }
            }
        });


        /**

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
         */


        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), uri);
        ringtone.play();


        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/Bariol_Regular.otf");


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


        CircleImageView theuserPhoto = findViewById(R.id.set_profile_image);


        Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/chatchat-da7fb.appspot.com/o/CHATELITE%2FPROFILES%2Fcropped5081725319317246073.jpg?alt=media&token=24bc6079-291b-4f21-a76a-717fcbbcd5d8").into(theuserPhoto);


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


    private class SinchCallListener implements CallListener {
        @Override
        public void onCallEnded(Call endedCall) {
            call = null;
            SinchError a = endedCall.getDetails().getError();
            button.setText("Call");
            callState.setText("");
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
        }

        @Override
        public void onCallEstablished(Call establishedCall) {
            callState.setText("connected");
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        }

        @Override
        public void onCallProgressing(Call progressingCall) {
            callState.setText("ringing");
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs) {
        }
    }

    private class SinchCallClientListener implements CallClientListener {
        @Override
        public void onIncomingCall(CallClient callClient, Call incomingCall) {
            call = incomingCall;
            Toast.makeText(VoiceCall.this, "incoming call", Toast.LENGTH_SHORT).show();
            call.answer();
            call.addCallListener(new SinchCallListener());
            button.setText("Hang Up");
        }
    }
}