package com.example.s1.mytaxi;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    EditText username, email, password, password2;
    Button btn_register;
    ProgressBar loadingProgress;
    DatabaseReference reference;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("회원가입");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        password2 = findViewById(R.id.password2);
        btn_register = findViewById(R.id.btn_register);
        loadingProgress = findViewById(R.id.regProgressBar);

        auth = FirebaseAuth.getInstance();
        loadingProgress.setVisibility(View.INVISIBLE);

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String txt_username = username.getText().toString();
                final String txt_email = email.getText().toString();
                final String txt_password = password.getText().toString();
                final String txt_password2 = password2.getText().toString();

                loadingProgress.setVisibility(View.VISIBLE);

                if(TextUtils.isEmpty(txt_username) || TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password) || TextUtils.isEmpty(txt_password2)){
                    Toast.makeText(RegisterActivity.this, "빈칸이 있습니다. 모두 채워주세요.", Toast.LENGTH_SHORT).show();
                    loadingProgress.setVisibility(View.INVISIBLE);
                } else if(!txt_password.equals(txt_password2)){
                    Toast.makeText(RegisterActivity.this, "비밀번호가 다릅니다.", Toast.LENGTH_SHORT).show();
                    loadingProgress.setVisibility(View.INVISIBLE);
                } else if(txt_password.length() < 6){
                    Toast.makeText(RegisterActivity.this, "비밀번호는 6글자 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
                    loadingProgress.setVisibility(View.INVISIBLE);
                } else {
                    register(txt_username,txt_email,txt_password);
                    Toast.makeText(RegisterActivity.this, "얼굴인식 후 회원가입이 완료됩니다.", Toast.LENGTH_SHORT).show();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Intent intent = new Intent(RegisterActivity.this, DetectActivity.class);
                    intent.putExtra("name", txt_username);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();

                }
            }
        });
    }

    public void register(final String username, final String email, final String password){

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser firebaseuser = auth.getCurrentUser();
                            assert firebaseuser != null;
                            String userid = firebaseuser.getUid();

                            reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("id",userid);
                            hashMap.put("username", username);
                            hashMap.put("imageURL", "default");

                            reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        //Toast.makeText(getApplicationContext(), "회원가입 성공", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else{
                            Toast.makeText(getApplicationContext(), "이미 등록된 이메일입니다.", Toast.LENGTH_SHORT).show();
                            //loadingProgress.setVisibility(View.INVISIBLE);
                            Intent intent = new Intent(getApplicationContext(),StartActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
    }
}
