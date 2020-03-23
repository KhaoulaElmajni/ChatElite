package com.chatelite.activities;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chatelite.fragments.PeerListDialogFragment;
import com.chatelite.R;

import org.json.JSONArray;

import java.util.ArrayList;

import io.skyway.Peer.Browser.Canvas;
import io.skyway.Peer.Browser.MediaConstraints;
import io.skyway.Peer.Browser.MediaStream;
import io.skyway.Peer.Browser.Navigator;
import io.skyway.Peer.CallOption;
import io.skyway.Peer.DataConnection;
import io.skyway.Peer.MediaConnection;
import io.skyway.Peer.OnCallback;
import io.skyway.Peer.Peer;
import io.skyway.Peer.PeerError;
import io.skyway.Peer.PeerOption;


public class VideoCall extends AppCompatActivity {
    private static final String TAG = VideoCall.class.getSimpleName();
    private static final String API_KEY = "e6344d24-2083-4814-80eb-cf25d839a415";
    private static final String DOMAIN = "localhost";

    private Peer _peer;
    private MediaStream _localStream;
    private MediaStream _remoteStream;
    private MediaConnection _mediaConnection;
    private DataConnection _signalingChannel;

    private String _strOwnId;

    public enum CallState {
        TERMINATED,
        CALLING,
        ESTABLISHED
    }

    private CallState _callState;

    private Handler _handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.video_call);
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


        _handler = new Handler(Looper.getMainLooper());
        final Activity activity = this;
        _callState = CallState.TERMINATED;

        //
        // Initialize Peer
        //
        PeerOption option = new PeerOption();
        option.key = API_KEY;
        option.domain = DOMAIN;
        _peer = new Peer(this, option);

        //
        // Set Peer event callbacks
        //

        // OPEN
        _peer.on(Peer.PeerEventEnum.OPEN, new OnCallback() {
            @Override
            public void onCallback(Object object) {

                // Show my ID
                _strOwnId = (String) object;
                //todo:
                //TextView tvOwnId = (TextView) findViewById(R.id.user_full_name);
                //tvOwnId.setText(_strOwnId);

                // Request permissions
                if (ContextCompat.checkSelfPermission(activity,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(activity,
                        Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, 0);
                } else {

                    // Get a local MediaStream & show it
                    startLocalStream();
                }

            }
        });

        // CALL (Incoming call)
        _peer.on(Peer.PeerEventEnum.CALL, new OnCallback() {
            @Override
            public void onCallback(Object object) {
                if (!(object instanceof MediaConnection)) {
                    return;
                }

                _mediaConnection = (MediaConnection) object;
                _callState = CallState.CALLING;
                showIncomingCallDialog();

            }
        });

        // CONNECT (Custom Signaling Channel for a call)
        _peer.on(Peer.PeerEventEnum.CONNECTION, new OnCallback() {
            @Override
            public void onCallback(Object object) {
                if (!(object instanceof DataConnection)) {
                    return;
                }

                _signalingChannel = (DataConnection) object;
                setSignalingCallbacks();

            }
        });

        _peer.on(Peer.PeerEventEnum.CLOSE, new OnCallback() {
            @Override
            public void onCallback(Object object) {
                Log.d(TAG, "[On/Close]");
            }
        });
        _peer.on(Peer.PeerEventEnum.DISCONNECTED, new OnCallback() {
            @Override
            public void onCallback(Object object) {
                Log.d(TAG, "[On/Disconnected]");
            }
        });
        _peer.on(Peer.PeerEventEnum.ERROR, new OnCallback() {
            @Override
            public void onCallback(Object object) {
                PeerError error = (PeerError) object;
                Log.d(TAG, "[On/Error]" + error.message);
            }
        });


        ImageView btnAction = findViewById(R.id.startCall);
        btnAction.setEnabled(true);
        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);

                if (CallState.TERMINATED == _callState) {

                    // Select remote peer & make a call
                    showPeerIDs();
                } else if (CallState.CALLING == _callState) {

                    // Cancel a call
                    if (null != _signalingChannel) {
                        _signalingChannel.send("cancel");
                    }
                    _callState = CallState.TERMINATED;
                    updateActionButtonTitle();

                } else {

                    // Hang up a call
                    closeRemoteStream();
                    _mediaConnection.close();
                    _signalingChannel.close();
                    _callState = CallState.TERMINATED;
                    updateActionButtonTitle();

                }

                v.setEnabled(true);
            }
        });
        //todo:
/**
 //ImageView switchCameraAction = findViewById(R.id.switchCamera);
 switchCameraAction.setOnClickListener(new View.OnClickListener() {
@Override public void onClick(View v)	{
if(null != _localStream){
Boolean result = _localStream.switchCamera();
if(true == result)	{
//Success
}
else {
//Failed
}
}

}
});
 **/


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
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocalStream();
                } else {
                    Toast.makeText(this, "Failed to access the camera and microphone.\nclick allow when asked for permission.", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Disable Sleep and Screen Lock
        Window wnd = getWindow();
        wnd.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        wnd.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Set volume control stream type to WebRTC audio.
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
    }

    @Override
    protected void onPause() {
        // Set default volume control stream type.
        setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
        super.onPause();
    }

    @Override
    protected void onStop() {
        // Enable Sleep and Screen Lock
        Window wnd = getWindow();
        wnd.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        wnd.clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        destroyPeer();
        super.onDestroy();
    }

    //
    // Get a local MediaStream & show it
    //
    void startLocalStream() {
        Navigator.initialize(_peer);
        MediaConstraints constraints = new MediaConstraints();
        _localStream = Navigator.getUserMedia(constraints);

        Canvas canvas = findViewById(R.id.svLocalView);
        _localStream.addVideoRenderer(canvas, 0);
    }

    //
    // Set callbacks for MediaConnection.MediaEvents
    //
    void setMediaCallbacks() {

        _mediaConnection.on(MediaConnection.MediaEventEnum.STREAM, new OnCallback() {
            @Override
            public void onCallback(Object object) {
                _remoteStream = (MediaStream) object;
                Canvas canvas = (Canvas) findViewById(R.id.svRemoteView);
                _remoteStream.addVideoRenderer(canvas, 0);
                _callState = CallState.ESTABLISHED;
                updateActionButtonTitle();
            }
        });

        _mediaConnection.on(MediaConnection.MediaEventEnum.CLOSE, new OnCallback() {
            @Override
            public void onCallback(Object object) {
                closeRemoteStream();
                _signalingChannel.close();
                _callState = CallState.TERMINATED;
                updateActionButtonTitle();
            }
        });

        _mediaConnection.on(MediaConnection.MediaEventEnum.ERROR, new OnCallback() {
            @Override
            public void onCallback(Object object) {
                PeerError error = (PeerError) object;
                Log.d(TAG, "[On/MediaError]" + error);
            }
        });

    }

    //
    // Set callbacks for DataConnection.DataEvents
    //
    void setSignalingCallbacks() {
        _signalingChannel.on(DataConnection.DataEventEnum.OPEN, new OnCallback() {
            @Override
            public void onCallback(Object object) {

            }
        });

        _signalingChannel.on(DataConnection.DataEventEnum.CLOSE, new OnCallback() {
            @Override
            public void onCallback(Object object) {

            }
        });

        _signalingChannel.on(DataConnection.DataEventEnum.ERROR, new OnCallback() {
            @Override
            public void onCallback(Object object) {
                PeerError error = (PeerError) object;
                Log.d(TAG, "[On/DataError]" + error);
            }
        });

        _signalingChannel.on(DataConnection.DataEventEnum.DATA, new OnCallback() {
            @Override
            public void onCallback(Object object) {
                String message = (String) object;
                Log.d(TAG, "[On/Data]" + message);

                switch (message) {
                    case "reject":
                        closeMediaConnection();
                        _signalingChannel.close();
                        _callState = CallState.TERMINATED;
                        updateActionButtonTitle();
                        break;
                    case "cancel":
                        closeMediaConnection();
                        _signalingChannel.close();
                        _callState = CallState.TERMINATED;
                        updateActionButtonTitle();
                        dismissIncomingCallDialog();
                        break;
                }
            }
        });

    }

    //
    // Clean up objects
    //
    private void destroyPeer() {
        closeRemoteStream();

        if (null != _localStream) {
            Canvas canvas = (Canvas) findViewById(R.id.svLocalView);
            _localStream.removeVideoRenderer(canvas, 0);
            _localStream.close();
        }

        closeMediaConnection();

        Navigator.terminate();

        if (null != _peer) {
            unsetPeerCallback(_peer);
            if (!_peer.isDisconnected()) {
                _peer.disconnect();
            }

            if (!_peer.isDestroyed()) {
                _peer.destroy();
            }

            _peer = null;
        }
    }

    //
    // Unset callbacks for PeerEvents
    //
    void unsetPeerCallback(Peer peer) {
        if (null == _peer) {
            return;
        }

        peer.on(Peer.PeerEventEnum.OPEN, null);
        peer.on(Peer.PeerEventEnum.CONNECTION, null);
        peer.on(Peer.PeerEventEnum.CALL, null);
        peer.on(Peer.PeerEventEnum.CLOSE, null);
        peer.on(Peer.PeerEventEnum.DISCONNECTED, null);
        peer.on(Peer.PeerEventEnum.ERROR, null);
    }

    //
    // Unset callbacks for MediaConnection.MediaEvents
    //
    void unsetMediaCallbacks() {
        if (null == _mediaConnection) {
            return;
        }

        _mediaConnection.on(MediaConnection.MediaEventEnum.STREAM, null);
        _mediaConnection.on(MediaConnection.MediaEventEnum.CLOSE, null);
        _mediaConnection.on(MediaConnection.MediaEventEnum.ERROR, null);
    }

    //
    // Close a MediaConnection
    //
    void closeMediaConnection() {
        if (null != _mediaConnection) {
            if (_mediaConnection.isOpen()) {
                _mediaConnection.close();
            }
            unsetMediaCallbacks();
        }
    }

    //
    // Close a remote MediaStream
    //
    void closeRemoteStream() {
        if (null == _remoteStream) {
            return;
        }

        Canvas canvas = (Canvas) findViewById(R.id.svRemoteView);
        _remoteStream.removeVideoRenderer(canvas, 0);
        _remoteStream.close();
    }

    //
    // Create a MediaConnection
    //
    void onPeerSelected(String strPeerId) {
        if (null == _peer) {
            return;
        }

        if (null != _mediaConnection) {
            _mediaConnection.close();
        }

        CallOption option = new CallOption();
        _mediaConnection = _peer.call(strPeerId, _localStream, option);
        if (null != _mediaConnection) {
            setMediaCallbacks();
            _callState = CallState.CALLING;
        }

        // custom P2P signaling channel to reject call attempt
        _signalingChannel = _peer.connect(strPeerId);
        if (null != _signalingChannel) {
            setSignalingCallbacks();
        }

        updateActionButtonTitle();
    }

    //
    // Listing all peers
    //
    void showPeerIDs() {
        if ((null == _peer) || (null == _strOwnId) || (0 == _strOwnId.length())) {
            Toast.makeText(this, "Your PeerID is null or invalid.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get all IDs connected to the server
        final Context fContext = this;
        _peer.listAllPeers(new OnCallback() {
            @Override
            public void onCallback(Object object) {
                if (!(object instanceof JSONArray)) {
                    return;
                }

                JSONArray peers = (JSONArray) object;
                ArrayList<String> _listPeerIds = new ArrayList<>();
                String peerId;

                // Exclude my own ID
                for (int i = 0; peers.length() > i; i++) {
                    try {
                        peerId = peers.getString(i);
                        if (!_strOwnId.equals(peerId)) {
                            _listPeerIds.add(peerId);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // Show IDs using DialogFragment
                if (0 < _listPeerIds.size()) {
                    FragmentManager mgr = getFragmentManager();
                    PeerListDialogFragment dialog = new PeerListDialogFragment();
                    dialog.setListener(
                            new PeerListDialogFragment.PeerListDialogFragmentListener() {
                                @Override
                                public void onItemClick(final String item) {
                                    _handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            onPeerSelected(item);
                                        }
                                    });
                                }
                            });
                    dialog.setItems(_listPeerIds);
                    dialog.show(mgr, "peerlist");
                } else {
                    Toast.makeText(fContext, "PeerID list (other than your ID) is empty.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    //
    // Update actionButton title
    //
    void updateActionButtonTitle() {
        _handler.post(new Runnable() {
            @Override
            public void run() {
               ImageView btnAction = findViewById(R.id.startCall);
                if (null != btnAction) {
                    if (CallState.TERMINATED == _callState) {
                        //btnAction.setText("Make Call");
                    } else if (CallState.CALLING == _callState) {
                        //btnAction.setText("Cancel");
                    } else {
                        //btnAction.setText("Hang up");
                    }
                }
            }
        });
    }

    //
    // Show alert dialog on an incoming call
    //
    AlertDialog incomingCallDialog;

    void showIncomingCallDialog() {
        incomingCallDialog = new AlertDialog.Builder(this)
                .setTitle("Incoming call")
                .setMessage("from : " + _mediaConnection.peer())
                .setPositiveButton("Answer", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        _mediaConnection.answer(_localStream);
                        setMediaCallbacks();
                        _callState = CallState.ESTABLISHED;
                        updateActionButtonTitle();
                    }
                })
                .setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (null != _signalingChannel) {
                            _signalingChannel.send("reject");
                            _callState = CallState.TERMINATED;
                        }
                    }
                })
                .show();
    }

    //
    // Dismiss alert dialog for an incoming call
    //
    void dismissIncomingCallDialog() {
        if (null != incomingCallDialog) {
            incomingCallDialog.cancel();
            incomingCallDialog = null;
        }
    }

}
