package com.chatelite.fragments;


import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chatelite.R;
import com.chatelite.activities.Discussion;
import com.chatelite.activities.GDiscussion;
import com.chatelite.activities.GroupDiscussion;
import com.chatelite.models.Contact;
import com.chatelite.models.Group;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class Groups extends Fragment {

    private View GroupsView;
    private RecyclerView myGroupsList;
    private DatabaseReference GroupsRef;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        GroupsView = inflater.inflate(R.layout.fragment_groups, container, false);
        myGroupsList = GroupsView.findViewById(R.id.groups_list);
        myGroupsList.setLayoutManager(new LinearLayoutManager(getContext()));
        mAuth = FirebaseAuth.getInstance();
        GroupsRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        return GroupsView;
    }

    @Override
    public void onStart() {
        super.onStart();


        ImageView photo = GroupsView.findViewById(R.id.no_item_photo);
        TextView text = GroupsView.findViewById(R.id.no_item_text);
        Typeface custom_font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Bariol_Regular.otf");
        text.setTypeface(custom_font);


        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Group>().setQuery(GroupsRef, Group.class).build();
        FirebaseRecyclerAdapter<Group, GroupsViewHolder> adapter = new FirebaseRecyclerAdapter<Group, GroupsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final GroupsViewHolder holder, int position, @NonNull Group model) {
                final String userIDs = getRef(position).getKey();
                GroupsRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            boolean memberOfTheGroup = true;
                            int membersNumber = 0;
                            for (DataSnapshot dataSnapshot1 : dataSnapshot.child("members").getChildren()) {
                                if (dataSnapshot1.child("id").getValue().toString().equals(mAuth.getCurrentUser().getUid())) {
                                    memberOfTheGroup = true;
                                    membersNumber = (int) dataSnapshot.child("members").getChildrenCount();
                                    break;
                                }
                            }

                            if (memberOfTheGroup) {
                                Typeface custom_font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Bariol_Regular.otf");
                                holder.groupName.setTypeface(custom_font);
                                holder.lastSentMessage.setTypeface(custom_font);
                                holder.membersNumber.setTypeface(custom_font);
                                holder.membersNumber.setText(membersNumber + " members");


                                holder.messagesNumber.setTypeface(custom_font);
                                holder.lastMessageDateAndTime.setTypeface(custom_font);
                                holder.archive.setTypeface(custom_font);




                                //TODO:
                                holder.lastSentMessage.setVisibility(View.GONE);
                                holder.lastMessageDateAndTime.setVisibility(View.GONE);
                                holder.ifSeen.setVisibility(View.GONE);
                                holder.messagesNumber.setVisibility(View.GONE);









                                String groupId = dataSnapshot.getKey();
                                String groupName = dataSnapshot.child("name").getValue().toString();
                                holder.groupName.setText(groupName);
                                holder.lastSentMessage.setText(groupName);
                                Picasso.get().load(dataSnapshot.child("photo").getValue().toString()).placeholder(R.drawable.profile_image).into(holder.groupPhoto);
                                String retImage = dataSnapshot.child("photo").getValue().toString();
                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        String currentGroupName = groupName;
                                        Intent groupChatIntent = new Intent(getContext(), GDiscussion.class);
                                        groupChatIntent.putExtra("groupName", currentGroupName);
                                        groupChatIntent.putExtra("groupId", groupId);
                                        groupChatIntent.putExtra("visit_user_image", retImage);
                                        startActivity(groupChatIntent);
                                    }
                                });

                                photo.setVisibility(View.GONE);
                                text.setVisibility(View.GONE);
                                myGroupsList.setVisibility(View.VISIBLE);
                            }

                        } else {
                            holder.layout.setVisibility(View.GONE);
                            photo.setVisibility(View.VISIBLE);
                            text.setVisibility(View.VISIBLE);
                            myGroupsList.setVisibility(View.GONE);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public GroupsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.group_layout, viewGroup, false);
                GroupsViewHolder viewHolder = new GroupsViewHolder(view);
                return viewHolder;
            }
        };
        myGroupsList.setAdapter(adapter);
        adapter.startListening();
    }


    public static class GroupsViewHolder extends RecyclerView.ViewHolder {
        TextView groupName, lastSentMessage, membersNumber, messagesNumber, lastMessageDateAndTime;
        CircleImageView groupPhoto;
        ImageView ifSeen;
        Button archive;
        LinearLayout layout;

        public GroupsViewHolder(@NonNull View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.groupName);
            lastSentMessage = itemView.findViewById(R.id.lastSentMessage);
            groupPhoto = itemView.findViewById(R.id.groupPhoto);
            membersNumber = itemView.findViewById(R.id.membersNumber);
            messagesNumber = itemView.findViewById(R.id.messagesNumber);
            lastMessageDateAndTime = itemView.findViewById(R.id.last_message_date_and_time);
            ifSeen = itemView.findViewById(R.id.ifSeen);
            archive = itemView.findViewById(R.id.archive);
            layout = itemView.findViewById(R.id.the_layout);
        }
    }

}
