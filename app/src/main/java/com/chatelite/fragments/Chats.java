package com.chatelite.fragments;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.MotionEventCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chatelite.R;
import com.chatelite.activities.Discussion;
import com.chatelite.models.Contact;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class Chats extends Fragment {
    private View PrivateChatsView;
    private RecyclerView chatsList;
    private DatabaseReference ChatsRef, UsersRef;
    private FirebaseAuth mAuth;
    private String currentUserID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        PrivateChatsView = inflater.inflate(R.layout.fragment_chats, container, false);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        ChatsRef = FirebaseDatabase.getInstance().getReference().child("Contact").child(currentUserID);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatsList = PrivateChatsView.findViewById(R.id.chats_list);
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));
        return PrivateChatsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        ImageView photo = PrivateChatsView.findViewById(R.id.no_item_photo);
        TextView text = PrivateChatsView.findViewById(R.id.no_item_text);
        Typeface  custom_font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Bariol_Regular.otf");
        text.setTypeface(custom_font);


        FirebaseRecyclerOptions<Contact> options = new FirebaseRecyclerOptions.Builder<Contact>()
                .setQuery(ChatsRef, Contact.class)
                .build();


        FirebaseRecyclerAdapter<Contact, ChatsViewHolder> adapter = new FirebaseRecyclerAdapter<Contact, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contact model) {
                final String usersIDs = getRef(position).getKey();
                final String[] retImage = {"default_image"};

                UsersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.hasChild("image")) {
                                retImage[0] = dataSnapshot.child("image").getValue().toString();
                                Picasso.get().load(retImage[0]).into(holder.profileImage);
                            }

                            //holder.itemView.setTag("");


                            final String retName = dataSnapshot.child("name").getValue().toString();


                            final String device_token = dataSnapshot.child("device_token").getValue().toString();
                            final String retStatus = dataSnapshot.child("status").getValue().toString();
                            holder.userName.setText(retName);
                            Typeface custom_font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Bariol_Regular.otf");
                            holder.userName.setTextColor(Color.BLACK);
                            holder.userName.setTypeface(custom_font);
                            if (dataSnapshot.child("userState").hasChild("state")) {
                                String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                String time = dataSnapshot.child("userState").child("time").getValue().toString();
                                holder.userLastMessage.setTypeface(custom_font);
                                holder.lastMessageDate.setTypeface(custom_font);
                                if (state.equals("Online")) {
                                    holder.lastMessageDate.setText("Online");
                                }else if (state.equals("Typing")) {
                                    holder.lastMessageDate.setText("Typing...");
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

                                    holder.lastMessageDate.setText(date + " at " + time);
                                }

                            } else {
                                holder.lastMessageDate.setText("Offline");

                            }


                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                            Query lastQuery = databaseReference.child("Message").child(currentUserID).child(usersIDs).orderByKey().limitToLast(1);
                            lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                            if (dataSnapshot1.hasChild("message")) {
                                                String message = dataSnapshot1.child("message").getValue().toString();
                                                holder.userLastMessage.setText(message);
                                            }

                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });


                            CountDownTimer countDownTimer = new CountDownTimer(500, 500) {

                                public void onTick(long millisUntilFinished) {

                                }

                                public void onFinish() {
                                    //Toast.makeText(getActivity(), "Long preess", Toast.LENGTH_SHORT).show();

                                    AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                                    builder1.setMessage("Write your message here.");
                                    builder1.setCancelable(true);

                                    builder1.setPositiveButton(
                                            "Delete chat",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.cancel();
                                                }
                                            });

                                    builder1.setNegativeButton(
                                            "Archive Chat",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.cancel();
                                                }
                                            });

                                    AlertDialog alert11 = builder1.create();
                                    alert11.show();

                                }

                            };


                            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                                @Override
                                public boolean onLongClick(View v) {
                                    AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                                    builder1.setMessage("Write your message here.");
                                    builder1.setCancelable(true);

                                    builder1.setPositiveButton(
                                            "Delete chat",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.cancel();
                                                }
                                            });

                                    builder1.setNegativeButton(
                                            "Archive Chat",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.cancel();
                                                }
                                            });

                                    AlertDialog alert11 = builder1.create();
                                    alert11.show();
                                    return false;
                                }
                            });

                            //TODO:
                            /*holder.theLayout.setOnTouchListener(new View.OnTouchListener() {
                                public boolean onTouch(View v, MotionEvent event) {
                                    switch (event.getAction()) {
                                        case MotionEvent.ACTION_DOWN:
                                            countDownTimer.start();
                                            break;

                                        case MotionEvent.ACTION_MOVE:

                                            break;

                                        case MotionEvent.ACTION_UP:
                                            countDownTimer.cancel();
                                            break;
                                    }
                                    return true;
                                }
                            });*/


                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent chatIntent = new Intent(getContext(), Discussion.class);
                                    chatIntent.putExtra("visit_user_id", usersIDs);
                                    chatIntent.putExtra("visit_user_name", retName);
                                    chatIntent.putExtra("visit_user_image", retImage[0]);
                                    chatIntent.putExtra("device_token", device_token);
                                    chatIntent.putExtra("user_full_name", device_token);
                                    startActivity(chatIntent);
                                }
                            });


                            photo.setVisibility(View.GONE);
                            text.setVisibility(View.GONE);
                            chatsList.setVisibility(View.VISIBLE);

                        }else{

                            photo.setVisibility(View.VISIBLE);
                            text.setVisibility(View.VISIBLE);
                            chatsList.setVisibility(View.GONE);
                        }



                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int j) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.last_discussion, viewGroup, false);
                Typeface custom_font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Bariol_Regular.otf");
                RelativeLayout layout = getActivity().findViewById(R.id.layout);
                ArrayList<View> clds = getAllChildren(layout);
                for (int i = 0; i < clds.size(); i += 1) {

                    if (clds.get(i) instanceof TextView) {
                        ((TextView) clds.get(i)).setTypeface(custom_font);
                    }

                    if (clds.get(i) instanceof Button) {
                        ((Button) clds.get(i)).setTypeface(custom_font);
                    }
                }
                return new ChatsViewHolder(view);
            }
        };
        chatsList.setAdapter(adapter);
        adapter.startListening();


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

    public static class ChatsViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView userLastMessage, userName, lastMessageDate;
        LinearLayout theLayout;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);
            theLayout = itemView.findViewById(R.id.the_layout);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            userLastMessage = itemView.findViewById(R.id.user_status);
            userName = itemView.findViewById(R.id.user_profile_name);
            lastMessageDate = itemView.findViewById(R.id.lastMessageDate);
        }
    }
}
