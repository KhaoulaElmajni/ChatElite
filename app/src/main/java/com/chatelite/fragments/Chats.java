package com.chatelite.fragments;


import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

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


    public Chats() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
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
                            Typeface custom_font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/font6.ttf");
                            holder.userName.setTextColor(Color.BLACK);
                            holder.userName.setTypeface(custom_font);
                            if (dataSnapshot.child("userState").hasChild("state")) {
                                String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                String time = dataSnapshot.child("userState").child("time").getValue().toString();
                                holder.userLastMessage.setTypeface(custom_font);
                                holder.lastMessageDate.setTypeface(custom_font);
                                if (state.equals("Online")) {
                                    holder.userLastMessage.setText("Online");
                                } else if (state.equals("Offline")) {
                                    Calendar calendar = Calendar.getInstance();
                                    SimpleDateFormat currentDate = new SimpleDateFormat("dd/MM/yyyy");
                                    String saveCurrentDate = currentDate.format(calendar.getTime());
                                    if (saveCurrentDate.equals(date))
                                        date = "Today";
                                    holder.lastMessageDate.setText(date + " at " + time);
                                }

                            } else {
                                holder.userLastMessage.setText("Offline");

                            }


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
                Typeface custom_font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/font6.ttf");
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

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            userLastMessage = itemView.findViewById(R.id.user_status);
            userName = itemView.findViewById(R.id.user_profile_name);
            lastMessageDate = itemView.findViewById(R.id.lastMessageDate);
        }
    }
}
