package com.pitechitsolutions.essentialtrailerhire;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private DatabaseReference databaseReference;
    private String branchId; // ID of the currently logged in branch
    // Firebase Auth
    private FirebaseAuth firebaseAuth;
    private ListView messagesView;
    private EditText messageBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        // Check if the user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if(currentUser != null) {
            branchId = currentUser.getUid();
        } else {
            // handle case when there is no user logged in
        }

        messagesView = findViewById(R.id.messages_view);
        messageBox = findViewById(R.id.message_box);

        Button sendButton = findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = messageBox.getText().toString();
                if (!message.isEmpty()) {
                    sendMessage(message);
                    messageBox.setText("");
                }
            }
        });

        loadMessages();
    }

    private void sendMessage(String message) {
        String chatId = "chats1";
        String messageId = databaseReference.child("chats").child(chatId).push().getKey();

        Map<String, Object> messageDetails = new HashMap<>();
        messageDetails.put("message", message);
        messageDetails.put("senderId", branchId);
        messageDetails.put("timestamp", System.currentTimeMillis());

        databaseReference.child("chats").child(chatId).child(messageId).setValue(messageDetails);
    }

    private void loadMessages() {
        String chatId = "chats1";
        databaseReference.child("chats").child(chatId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<ChatMessage> messages = new ArrayList<>();
                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    ChatMessage message = messageSnapshot.getValue(ChatMessage.class);
                    messages.add(message);
                }

                ArrayAdapter<ChatMessage> adapter = new ArrayAdapter<ChatMessage>(ChatActivity.this, R.layout.message_item, messages) {
                    @NonNull
                    @Override
                    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        if (convertView == null) {
                            convertView = LayoutInflater.from(getContext()).inflate(R.layout.message_item, parent, false);
                        }

                        TextView messageText = convertView.findViewById(R.id.message_text);
                        TextView branchNameView = convertView.findViewById(R.id.branch_name);
                        TextView timestamp = convertView.findViewById(R.id.timestamp);

                        ChatMessage message = getItem(position);
                        messageText.setText(message.getMessage());

                        // Handle the branch name
                        String senderId = message.getSenderId();
                        if (senderId != null) {
                            getBranchName(senderId, new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        String branchNameString = dataSnapshot.child("branchName").getValue(String.class);
                                        branchNameView.setText(branchNameString);
                                    } else {
                                        branchNameView.setText("Branch not found");
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    // Handle possible errors.
                                }
                            });
                        } else {
                            branchNameView.setText("Unknown sender");
                        }

                        // Convert timestamp to human-readable date string
                        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        String dateString = df.format(new Date(message.getTimestamp()));
                        timestamp.setText(dateString);

                        return convertView;
                    }
                };

                messagesView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("LoadMessages", "Failed to load messages: ", error.toException());
                Toast.makeText(ChatActivity.this, "Failed to load messages.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getBranchName(String branchId, ValueEventListener listener) {
        databaseReference.child("branches").child(branchId).addListenerForSingleValueEvent(listener);
    }
}
