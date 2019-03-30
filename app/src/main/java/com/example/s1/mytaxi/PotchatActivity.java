package com.example.s1.mytaxi;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.s1.mytaxi.Adapter.PotchatAdapter;
import com.example.s1.mytaxi.Fragments.APIService;
import com.example.s1.mytaxi.Model.Potchat;
import com.example.s1.mytaxi.Model.User;
import com.example.s1.mytaxi.Notifications.Client;
import com.example.s1.mytaxi.Notifications.Data;
import com.example.s1.mytaxi.Notifications.MyResponse;
import com.example.s1.mytaxi.Notifications.Sender;
import com.example.s1.mytaxi.Notifications.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PotchatActivity extends AppCompatActivity {

    CircleImageView pot_image;
    TextView potname;

    FirebaseUser fuser;
    DatabaseReference reference;

    ImageButton btn_send;
    EditText text_send;

    RecyclerView recyclerView;

    PotchatAdapter potchatAdapter;
    ValueEventListener seenListener;

    String username;
    String url;
    private String pot_name;
    private List<String> mUsers;

    Intent intent;
    boolean notify = false;

    APIService apiService;
    List<Potchat> mchat;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // and this
                startActivity(new Intent(PotchatActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        pot_image = findViewById(R.id.profile_image);
        potname = findViewById(R.id.username);
        btn_send = findViewById(R.id.btn_send);
        text_send = findViewById(R.id.text_send);

        intent = getIntent();
        pot_name = intent.getStringExtra("potname");
        Log.d("potname: ",pot_name+"\n=================================\n");
        fuser = FirebaseAuth.getInstance().getCurrentUser();

        potname.setText(pot_name);
        pot_image.setImageResource(R.mipmap.ic_launcher);
        readMsg(pot_name);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username = user.getUsername();
                url = user.getImageURL();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify = true;
                String msg = text_send.getText().toString();
                if (!msg.equals("")) {
                    sendMessage(fuser.getUid(), pot_name, msg, url, username);
                } else {
                    Toast.makeText(PotchatActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
                }
                text_send.setText("");
            }
        });

    }

    private void seenMessage(final String pot_name) {
        reference = FirebaseDatabase.getInstance().getReference("Potchat");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Potchat chat = snapshot.getValue(Potchat.class);
                    if (chat.getReceiver().equals(pot_name)&&!chat.getSender().equals(fuser.getUid())) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMsg(final String myid) {
        mchat = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Potchats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mchat.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Potchat chat = snapshot.getValue(Potchat.class);
                    if (chat.getReceiver().equals(myid)) {
                        mchat.add(chat);
                    }

                    potchatAdapter = new PotchatAdapter(PotchatActivity.this, mchat);
                    recyclerView.setAdapter(potchatAdapter);
                }
                seenMessage(myid);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(String sender, final String pot_name, final String message, final String url, final String username) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", pot_name);
        hashMap.put("message", message);
        hashMap.put("url", url);
        hashMap.put("username", username);
        hashMap.put("isseen", false);

        reference.child("Potchats").push().setValue(hashMap);


        // add user to chat fragment
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(fuser.getUid())
                .child(pot_name);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    chatRef.child("pot_name").setValue(pot_name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (notify) {
                    sendNotifiaction(pot_name, user.getUsername(), message);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendNotifiaction(final String pot_name, final String username, final String message) {
        mUsers = new ArrayList<>();
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(pot_name);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);
                    Data data = null;
                    DatabaseReference chatrec = FirebaseDatabase.getInstance().getReference("Chatlist").child(pot_name);
                    chatrec.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            mUsers.clear();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                User user = snapshot.getValue(User.class);

                                assert user != null;
                                assert fuser != null;
                                if (!user.getId().equals(fuser.getUid())) {
                                    mUsers.add(user.getId());
                                }
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    Iterator iterator = mUsers.iterator();
                    while (iterator.hasNext()) {
                        data = new Data(fuser.getUid(), R.mipmap.ic_launcher, pot_name + "의 " + username + ": " + message, "New Message",
                                (String) iterator.next());
                    }


                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success != 1) {
                                            Toast.makeText(PotchatActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}