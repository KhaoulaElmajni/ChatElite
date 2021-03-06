package com.chatelite.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chatelite.R;
import com.chatelite.activities.ImageViewer;
import com.chatelite.activities.Main;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Message extends RecyclerView.Adapter<Message.MessageViewHolder> {
    private List<com.chatelite.models.Message> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;
    Typeface custom_font;
    String date = null;
    HashMap<String, ImageView> imageViews;

    public Message(Context context, List<com.chatelite.models.Message> userMessagesList) {
        this.userMessagesList = userMessagesList;
        custom_font = Typeface.createFromAsset(context.getAssets(), "fonts/Tajawal-Regular.ttf");
        imageViews = new HashMap<>();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView senderMessageText, receiverMessageText, sentTime, secondSentTime, messagesDate;
        public ImageView messageSenderPicture, messageReceiverPicture, seen;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMessageText = itemView.findViewById(R.id.sender_message_text);
            sentTime = itemView.findViewById(R.id.message_time);
            secondSentTime = itemView.findViewById(R.id.second_message_time);
            receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view);
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view);
            seen = itemView.findViewById(R.id.seen);
            messagesDate = itemView.findViewById(R.id.messages_date);
            Log.d("MY-CHATELIE", "CALLED !");
        }
    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.message_layout, viewGroup, false);
        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, final int position) {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        com.chatelite.models.Message messages = userMessagesList.get(position);
        Log.d("estsb", position + "");
        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();
        Log.d("estsb", fromUserID + "");
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("image")) {
                    String receiverImage = dataSnapshot.child("image").getValue().toString();

                    //Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(messageViewHolder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        messageViewHolder.receiverMessageText.setVisibility(View.GONE);
        messageViewHolder.senderMessageText.setVisibility(View.GONE);
        messageViewHolder.messageSenderPicture.setVisibility(View.GONE);
        messageViewHolder.messageReceiverPicture.setVisibility(View.GONE);
        messageViewHolder.messagesDate.setTypeface(custom_font);


        if (position == 0) {
            messageViewHolder.messagesDate.setVisibility(View.VISIBLE);
            date = messages.getDate();
            messageViewHolder.messagesDate.setText(date);
        } else {
            messageViewHolder.messagesDate.setVisibility(View.GONE);
            if (!date.equals(messages.getDate())) {
                messageViewHolder.messagesDate.setVisibility(View.VISIBLE);
                date = messages.getDate();
                messageViewHolder.messagesDate.setText(date);
            }
            date = messages.getDate();
        }

        messageViewHolder.messagesDate.setTypeface(custom_font);


        if (fromMessageType.equals("text")) {

            if (fromUserID.equals(messageSenderId)) {


                messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.secondSentTime.setVisibility(View.INVISIBLE);
                messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                messageViewHolder.senderMessageText.setTextColor(Color.BLACK);
                messageViewHolder.senderMessageText.setText(messages.getMessage());
                messageViewHolder.sentTime.setText(messages.getTime());
                messageViewHolder.senderMessageText.setTypeface(custom_font);
                messageViewHolder.sentTime.setTypeface(custom_font);
                messageViewHolder.messagesDate.setTypeface(custom_font);
                messageViewHolder.messagesDate.setTypeface(custom_font);
                messageViewHolder.seen.setTag(messages.getMessageID());
                imageViews.put(messages.getMessageID(), messageViewHolder.seen);

                FirebaseDatabase.getInstance().getReference().child("Message")
                        .child(messages.getFrom()).child(messages.getTo()).
                        child(messages.getMessageID()).addValueEventListener(new ValueEventListener() {
                    String tag = messages.getMessageID();

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ImageView imageView = imageViews.get(tag);

                        if (imageView != null) {
                            if (dataSnapshot.child("MessageState").getValue().toString().equals("SENT")) {
                                imageView.setImageResource(R.drawable.sent_state);
                            } else if (dataSnapshot.child("MessageState").getValue().toString().equals("SEEN")) {
                                imageView.setImageResource(R.drawable.seen);
                            } else if (dataSnapshot.child("MessageState").getValue().toString().equals("DELIVERED")) {
                                imageView.setImageResource(R.drawable.doublee);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



                /*messageViewHolder.senderMessageText.setOnTouchListener(new View.OnTouchListener() {
                    float dX, initialX, initialY;
                    boolean isInitialPositionSet = false;
                    float dY;
                    int lastAction;

                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        if (!isInitialPositionSet) {
                            initialX = view.getX();
                            initialY = view.getY();
                            isInitialPositionSet = true;
                        }
                        switch (event.getActionMasked()) {
                            case MotionEvent.ACTION_DOWN:
                                dX = view.getX() - event.getRawX();
                                dY = view.getY() - event.getRawY();
                                lastAction = MotionEvent.ACTION_DOWN;
                                break;

                            case MotionEvent.ACTION_MOVE:
                                //view.setY(event.getRawY() + dY);
                                view.setX(event.getRawX() + dX);
                                lastAction = MotionEvent.ACTION_MOVE;
                                break;

                            case MotionEvent.ACTION_UP:
                                //if (lastAction == MotionEvent.ACTION_DOWN)
                                view.setX(initialX);
                                break;

                            case MotionEvent.ACTION_CANCEL:
                                //if (lastAction == MotionEvent.ACTION_DOWN)
                                view.setX(initialX);
                                break;

                            default:
                                return false;
                        }
                        return true;
                    }
                });
*/

            } else {
                FirebaseDatabase.getInstance().getReference().child("Message").child(fromUserID).child(messageSenderId).child(messages.getMessageID()).child("MessageState").setValue("SEEN");
                FirebaseDatabase.getInstance().getReference().child("Message").child(messageSenderId).child(fromUserID).child(messages.getMessageID()).child("MessageState").setValue("SEEN");

                messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.sentTime.setVisibility(View.INVISIBLE);
                messageViewHolder.seen.setVisibility(View.INVISIBLE);
                messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                messageViewHolder.receiverMessageText.setTextColor(Color.BLACK);
                messageViewHolder.receiverMessageText.setTypeface(custom_font);
                messageViewHolder.secondSentTime.setText(messages.getTime());
                messageViewHolder.receiverMessageText.setText(messages.getMessage());
                messageViewHolder.secondSentTime.setTypeface(custom_font);
            }

        } else if (fromMessageType.equals("image")) {


            if (fromUserID.equals(messageSenderId)) {
                messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);
                messageViewHolder.secondSentTime.setVisibility(View.INVISIBLE);
                Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageSenderPicture);
                messageViewHolder.senderMessageText.setVisibility(View.GONE);
                messageViewHolder.secondSentTime.setVisibility(View.INVISIBLE);
                messageViewHolder.sentTime.setText(messages.getTime());
                messageViewHolder.sentTime.setTypeface(custom_font);

            } else {
                messageViewHolder.seen.setVisibility(View.GONE);
                messageViewHolder.senderMessageText.setVisibility(View.GONE);
                messageViewHolder.secondSentTime.setVisibility(View.VISIBLE);
                messageViewHolder.secondSentTime.setText(messages.getTime());
                messageViewHolder.secondSentTime.setTypeface(custom_font);
                messageViewHolder.sentTime.setVisibility(View.GONE);
                messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageReceiverPicture);
            }
        } else if (fromMessageType.equals("pdf") || fromMessageType.equals("docx")) {
            if (fromUserID.equals(messageSenderId)) {
                messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);

                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/chatchat-da7fb.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=5a7c0cfe-1ef2-4f2d-a07e-57cdb18f30a6")
                        .into(messageViewHolder.messageSenderPicture);
                messageViewHolder.secondSentTime.setVisibility(View.GONE);
                messageViewHolder.senderMessageText.setVisibility(View.GONE);
                messageViewHolder.sentTime.setVisibility(View.VISIBLE);
                messageViewHolder.sentTime.setText(messages.getTime());
                messageViewHolder.sentTime.setTypeface(custom_font);
            } else {
                messageViewHolder.seen.setVisibility(View.GONE);
                messageViewHolder.sentTime.setVisibility(View.GONE);
                messageViewHolder.receiverMessageText.setVisibility(View.GONE);
                messageViewHolder.secondSentTime.setVisibility(View.VISIBLE);
                messageViewHolder.secondSentTime.setText(messages.getTime());
                messageViewHolder.secondSentTime.setTypeface(custom_font);
                messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/chatchat-da7fb.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=5a7c0cfe-1ef2-4f2d-a07e-57cdb18f30a6")
                        .into(messageViewHolder.messageReceiverPicture);
            }
        }


        if (fromUserID.equals(messageSenderId)) {
            messageViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx")) {
                        CharSequence options[] = new CharSequence[]{
                                "Delete For me",
                                "Download and View this Document",
                                "Cancel",
                                "Delete For everyone"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0) {
                                    deleteSentMessage(position, messageViewHolder);

                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), Main.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                } else if (i == 1) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                } else if (i == 3) {
                                    deleteMessageForEveryone(position, messageViewHolder);

                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), Main.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    } else if (userMessagesList.get(position).getType().equals("text")) {
                        CharSequence options[] = new CharSequence[]{
                                "Delete For me",
                                "Cancel",
                                "Delete For everyone"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0) {
                                    deleteSentMessage(position, messageViewHolder);

                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), Main.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                } else if (i == 2) {
                                    deleteMessageForEveryone(position, messageViewHolder);

                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), Main.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });
                        builder.show();
                    } else if (userMessagesList.get(position).getType().equals("image")) {
                        CharSequence options[] = new CharSequence[]{
                                "Delete For me",
                                "View this Image",
                                "Cancel",
                                "Delete For everyone"
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0) {
                                    deleteSentMessage(position, messageViewHolder);

                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), Main.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                } else if (i == 1) {
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), ImageViewer.class);
                                    intent.putExtra("url", userMessagesList.get(position).getMessage());
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                } else if (i == 3) {
                                    deleteMessageForEveryone(position, messageViewHolder);

                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), Main.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                    return false;
                }
            });
        } else {


            messageViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx")) {
                        CharSequence options[] = new CharSequence[]{
                                "Delete For me",
                                "Download and View this Document",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0) {
                                    deleteReceiveMessage(position, messageViewHolder);

                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), Main.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                } else if (i == 1) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });
                        builder.show();
                    } else if (userMessagesList.get(position).getType().equals("text")) {
                        CharSequence options[] = new CharSequence[]{
                                "Delete For me",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0) {
                                    deleteReceiveMessage(position, messageViewHolder);

                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), Main.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });
                        builder.show();
                    } else if (userMessagesList.get(position).getType().equals("image")) {
                        CharSequence options[] = new CharSequence[]{
                                "Delete For me",
                                "View this Image",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0) {
                                    deleteReceiveMessage(position, messageViewHolder);

                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), Main.class);
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                } else if (i == 1) {
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), ImageViewer.class);
                                    intent.putExtra("url", userMessagesList.get(position).getMessage());
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                    return false;
                }
            });


        }

    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    private void deleteSentMessage(final int position, final MessageViewHolder holder) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Message").child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(holder.itemView.getContext(), "ERROR Occurred.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteReceiveMessage(final int position, final MessageViewHolder holder) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Message").child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(holder.itemView.getContext(), "ERROR Occurred.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteMessageForEveryone(final int position, final MessageViewHolder holder) {
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Message").child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID()).child("message")
                .setValue("This message has been deleted !").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    rootRef.child("Message").child(userMessagesList.get(position).getFrom())
                            .child(userMessagesList.get(position).getTo())
                            .child(userMessagesList.get(position).getMessageID())
                            .child("message")
                            .setValue("This message has been deleted !").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(holder.itemView.getContext(), "Deleted Successfully.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } else {
                    Toast.makeText(holder.itemView.getContext(), "ERROR Occurred.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
