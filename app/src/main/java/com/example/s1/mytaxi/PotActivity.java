package com.example.s1.mytaxi;

import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.s1.mytaxi.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.HashMap;

public class PotActivity extends AppCompatActivity {

    TextView dep, dest, text_date;
    Button btn_dep, btn_dest, btn_pot;
    ProgressBar loadingProgress;
    String username, gender;
    String lat, lon;
    private RadioGroup radioGroup1, radioGroup2;
    String male, date;
    EditText edit_date;

    private int REQUEST_DEP = 1;
    private int REQUEST_DEST = 2;
    FirebaseUser fuser;
    DatabaseReference reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pot);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("팟 작성");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // and this
                startActivity(new Intent(PotActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });
        date = null;
        dep = findViewById(R.id.start);
        dest = findViewById(R.id.finish);
        btn_pot = findViewById(R.id.btn_pot);
        btn_dep = findViewById(R.id.btn_dep);
        btn_dest = findViewById(R.id.btn_dest);
        edit_date = findViewById(R.id.edit_date);
        text_date = findViewById(R.id.text_date);
        loadingProgress = findViewById(R.id.regProgressBar);
        radioGroup1 = findViewById(R.id.radioGroup1);
        radioGroup2 = findViewById(R.id.radioGroup2);
        radioGroup1.setOnCheckedChangeListener(radioGroupButtonChanegeListner);
        radioGroup2.setOnCheckedChangeListener(radioGroupButtonChanegeListner2);

        loadingProgress.setVisibility(View.INVISIBLE);
        fuser = FirebaseAuth.getInstance().getCurrentUser();


        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);

                    if (user.getId()!=null&&user.getId().equals(fuser.getUid())) {
                        username = user.getUsername();
                        gender = user.getGender();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        btn_dep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PotActivity.this, LocationActivity.class);
                intent.putExtra("location",REQUEST_DEP);
                startActivityForResult(intent, REQUEST_DEP);
            }
        });
        btn_dest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PotActivity.this, LocationActivity.class);
                intent.putExtra("location",REQUEST_DEST);
                startActivityForResult(intent, REQUEST_DEST);
            }
        });

        btn_pot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String txt_start = dep.getText().toString();
                final String txt_dest = dest.getText().toString();
                final String txt_id = fuser.getUid();
                final String txt_name = username;
                String txt_male;
                String txt_date;
                loadingProgress.setVisibility(View.VISIBLE);
                if (male.equals("same"))
                    txt_male = gender;
                else
                    txt_male = male;

                if(TextUtils.isEmpty(date))
                    txt_date = edit_date.getText().toString();
                else
                    txt_date = date;

                if(TextUtils.isEmpty(txt_start) || TextUtils.isEmpty(txt_dest)|| TextUtils.isEmpty(txt_male)||TextUtils.isEmpty(txt_date)){
                    Toast.makeText(PotActivity.this, "빈칸이 있습니다.", Toast.LENGTH_SHORT).show();
                    loadingProgress.setVisibility(View.INVISIBLE);
                }else{
                    pot(txt_start, txt_dest, txt_id, txt_name, txt_male, lat, lon, txt_date);
                }
            }
        });
    }

    RadioGroup.OnCheckedChangeListener radioGroupButtonChanegeListner = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup1, @IdRes int i) {
            if(i== R.id.radio0) {
                text_date.setVisibility(View.INVISIBLE);
                edit_date.setVisibility(View.VISIBLE);
                date = "";
            }else if(i==R.id.radio1){
                SimpleDateFormat format2 = new SimpleDateFormat("MM월 dd일");
                edit_date.setVisibility(View.INVISIBLE);
                text_date.setVisibility(View.VISIBLE);
                text_date.setText(format2.format(System.currentTimeMillis()));
                date = format2.format(System.currentTimeMillis());
            }
        }
    };

    RadioGroup.OnCheckedChangeListener radioGroupButtonChanegeListner2 = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup1, @IdRes int i) {
            if(i== R.id.radio2) {
                male = "same";
            }else if(i==R.id.radio3){
                male = " ";
            }
        }
    };

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_DEP) {
            if (resultCode == RESULT_OK) {
                dep.setText(data.getStringExtra("location"));
                lat = data.getStringExtra("lat");
                lon = data.getStringExtra("lon");
            } else {
                Toast.makeText(PotActivity.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_DEST) {
            if (resultCode == RESULT_OK) {
                dest.setText(data.getStringExtra("location"));
            } else {
                Toast.makeText(PotActivity.this, "Failed", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void pot(String dep, String dest, String userid, String name, String male, final String lat, final String lon, String date) {
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        final String dt = date;
        final String format_time = format1.format (System.currentTimeMillis());

        reference = FirebaseDatabase.getInstance().getReference("Potlist").child(userid+format_time);
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("dep", dep);
        hashMap.put("dest", dest);
        hashMap.put("userid", userid);
        hashMap.put("username", name);
        hashMap.put("time", format_time);
        hashMap.put("male", male);
        hashMap.put("date", dt);
        hashMap.put("lat", lat);
        hashMap.put("lon", lon);
        reference = FirebaseDatabase.getInstance().getReference("Potlist").child(userid+format_time);

        DatabaseReference chatRefReceiver = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(fuser.getUid()).child(dest+"("+dt+")");
        chatRefReceiver.child("pot_name").setValue(dest+"("+dt+")");

        reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Intent intent = new Intent(PotActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
                else{
                    Toast.makeText(PotActivity.this, "You can't make pot", Toast.LENGTH_SHORT).show();
                    loadingProgress.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
}
