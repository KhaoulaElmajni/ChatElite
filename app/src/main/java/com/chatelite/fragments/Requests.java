package com.chatelite.fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.TextView;
import android.widget.Toast;

import com.chatelite.R;
import com.chatelite.models.Contact;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class Requests extends Fragment {
    private View RequestsFragmentView;
    private RecyclerView myRequestsList;
    private DatabaseReference ChatRequestsRef, UsersRef, ContactsRef;
    private FirebaseAuth mAuth;
    private String currentUserID;

    public Requests() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RequestsFragmentView = inflater.inflate(R.layout.fragment_requests, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestsRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contact");
        myRequestsList = RequestsFragmentView.findViewById(R.id.chat_request_list);
        myRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return RequestsFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contact> options = new FirebaseRecyclerOptions.Builder<Contact>()
                .setQuery(ChatRequestsRef.child(currentUserID), Contact.class).build();


        FirebaseRecyclerAdapter<Contact, RequsestsViewHolder> adapter = new
                FirebaseRecyclerAdapter<Contact, RequsestsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final RequsestsViewHolder holder, int position, @NonNull Contact model) {
                        holder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.VISIBLE);
                        holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.VISIBLE);


                        Typeface custom_font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/font6.ttf");
                        holder.userName.setTypeface(custom_font);
                        holder.userStatus.setTypeface(custom_font);
                        holder.AcceptButton.setTypeface(custom_font);
                        holder.AcceptButton.setTypeface(custom_font);

                        final String list_user_id = getRef(position).getKey();
                        DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();

                        getTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    String type = dataSnapshot.getValue().toString();
                                    // Log.d("estsb",type);

                                    if (type.equals("received")) {
                                        UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.hasChild("image")) {

                                                    final String requestProfileImage = dataSnapshot.child("image").getValue().toString();


                                                    Picasso.get().load(requestProfileImage).into(holder.profileImage);

                                                }

                                                final String requestUserName = dataSnapshot.child("name").getValue().toString();
                                                final String requestUserStatus = dataSnapshot.child("status").getValue().toString();

                                                holder.userName.setText(requestUserName);
                                                holder.userStatus.setText("Wants to connect with you");



                                                Button accept = holder.AcceptButton;
                                                Typeface custom_font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/font6.ttf");
                                                accept.setTypeface(custom_font);
                                                accept.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {

                                                            ContactsRef.child(list_user_id).child(currentUserID).child("Contact")
                                                                    .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        ContactsRef.child(currentUserID).child(list_user_id).child("Contact")
                                                                                .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()) {
                                                                                    ChatRequestsRef.child(list_user_id).child(currentUserID)
                                                                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if (task.isSuccessful()) {
                                                                                                ChatRequestsRef.child(currentUserID).child(list_user_id)
                                                                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                        if (task.isSuccessful()) {
                                                                                                            Toast.makeText(getContext(), "New Contact Saved...", Toast.LENGTH_SHORT).show();
                                                                                                        }
                                                                                                    }
                                                                                                });
                                                                                            }
                                                                                        }
                                                                                    });
                                                                                }
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });


                                                    }
                                                });


                                                Button cancel = holder.CancelButton;
                                                //Typeface custom_font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/font6.ttf");
                                                cancel.setTypeface(custom_font);
                                                cancel.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {

                                                            ChatRequestsRef.child(list_user_id).child(currentUserID)
                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        ChatRequestsRef.child(currentUserID).child(list_user_id)
                                                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()) {
                                                                                    Toast.makeText(getContext(), "Contact Deleted...", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });

                                                    }
                                                });

                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        CharSequence options[] = new CharSequence[]{"Accept", "Cancel"};
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                        builder.setTitle(requestUserName + " Chat Request");
                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                           //TODO:
                                                            }
                                                        });

                                                        builder.show();
                                                    }
                                                });

                                            }


                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    } else if (type.equals("sent")) {
                                        Button request_sent_btn = holder.itemView.findViewById(R.id.request_cancel_btn);
                                        request_sent_btn.setText("Cancel");
                                        Typeface custom_font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/font6.ttf");
                                        request_sent_btn.setTypeface(custom_font);
                                        request_sent_btn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                ChatRequestsRef.child(list_user_id).child(currentUserID)
                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            ChatRequestsRef.child(currentUserID).child(list_user_id)
                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        Toast.makeText(getContext(), "You have cancelled the chat request...", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }
                                        });
                                        //TODO: INVISIBLE
                                        holder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.GONE);
                                        //TODO: here start ! be carefull
                                        UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.hasChild("image")) {

                                                    final String requestProfileImage = dataSnapshot.child("image").getValue().toString();


                                                    Picasso.get().load(requestProfileImage).into(holder.profileImage);

                                                }

                                                final String requestUserName = dataSnapshot.child("name").getValue().toString();
                                                final String requestUserStatus = dataSnapshot.child("status").getValue().toString();

                                                holder.userName.setText(requestUserName);
                                                holder.userStatus.setText("you have sent a request to " + requestUserName);


                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        CharSequence options[] = new CharSequence[]{"Cancel the Chat Request"};
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                        builder.setTitle("Already Sent Request");
                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {

                                                                if (i == 0) {
                                                                    //TODO:
                                                                }
                                                            }
                                                        });

                                                        builder.show();
                                                    }
                                                });

                                            }


                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });

                                    }


                                }


                                else{
                                    myRequestsList.setVisibility(View.GONE);
                                    ImageView photo =  RequestsFragmentView.findViewById(R.id.no_item_photo);
                                    TextView text =  RequestsFragmentView.findViewById(R.id.no_item_text);
                                    photo.setVisibility(View.VISIBLE);
                                    text.setVisibility(View.VISIBLE);

                                    Typeface custom_font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/font6.ttf");
                                   text.setTypeface(custom_font);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }

                    @NonNull
                    @Override
                    public RequsestsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_layout_in_requests, viewGroup, false);
                        RequsestsViewHolder holder = new RequsestsViewHolder(view);
                        return holder;
                    }
                };

        myRequestsList.setAdapter(adapter);
        adapter.startListening();

    }

    public static class RequsestsViewHolder extends RecyclerView.ViewHolder {

        TextView userName, userStatus;
        CircleImageView profileImage;
        Button AcceptButton, CancelButton;

        public RequsestsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            AcceptButton = itemView.findViewById(R.id.request_accept_btn);
            CancelButton = itemView.findViewById(R.id.request_cancel_btn);


        }
    }
}