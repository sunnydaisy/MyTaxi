package com.example.s1.mytaxi;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.s1.mytaxi.Adapter.PotAdapter;
import com.example.s1.mytaxi.Detector.Classifier;
import com.example.s1.mytaxi.Detector.TensorFlowImageClassifier;
import com.example.s1.mytaxi.Fragments.PotFragment;
import com.example.s1.mytaxi.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GenderActivity extends AppCompatActivity {
    private static final String MODEL_FILE = "file:///android_asset/stripped_graph.pb";
    private static final String LABEL_FILE = "file:///android_asset/output_labels.txt";

    TextView detect;
    TextView textView;
    String myge, title, gender;
    Bitmap image;

    private static final int INPUT_SIZE = 299;
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128;
    private int pot;
    private static final String INPUT_NAME = "Mul";
    private static final String OUTPUT_NAME = "final_result";
    private Classifier classifier;
    private Executor executor = Executors.newSingleThreadExecutor();

   // ImageView BigImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gender);

        byte[] arr = getIntent().getByteArrayExtra("image");
        if(getIntent().getIntExtra("pot", 0)==1){
            pot = 1;
            gender = getIntent().getStringExtra("gender");
            title = getIntent().getStringExtra("title");
        }
        image = BitmapFactory.decodeByteArray(arr, 0, arr.length);
        int width = image.getWidth();
        int height = image.getHeight();
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap resizedBitmap = Bitmap.createBitmap(image,0,0,width,height,matrix,true);
        image = Bitmap.createScaledBitmap(image, 299, 299, true);
        textView = findViewById(R.id.textView);
        //BigImage = (ImageView) findViewById(R.id.photo);
        //BigImage.setImageBitmap(image);

        detect = findViewById(R.id.detect);

        initTensorFlowAndLoadModel();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        recognize_bitmap(image);

    }
    private void initTensorFlowAndLoadModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = TensorFlowImageClassifier.create(
                            getAssets(),
                            MODEL_FILE,
                            LABEL_FILE,
                            INPUT_SIZE,
                            IMAGE_MEAN,
                            IMAGE_STD,
                            INPUT_NAME,
                            OUTPUT_NAME);
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }
    private void recognize_bitmap(Bitmap bitmap) {
        final FirebaseUser firebaseuser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseuser != null;

        String userid = firebaseuser.getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        HashMap<String, Object> hashMap = new HashMap<>();
        // 비트맵을 처음에 정의된 INPUT SIZE에 맞춰 스케일링 (상의 왜곡이 일어날수 있는데, 이건 나중에 따로 설명할게요)
        //bitmap = Bitmap.createScaledBitmap(bitmap, 299, 299, false);

        // classifier 의 recognizeImage 부분이 실제 inference 를 호출해서 인식작업을 하는 부분입니다.
        final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);
        // 결과값은 Classifier.Recognition 구조로 리턴되는데, 원래는 여기서 결과값을 배열로 추출가능하지만,
        // 여기서는 간단하게 그냥 통째로 txtResult에 뿌려줍니다.
        // imgResult에는 분석에 사용된 비트맵을 뿌려줍니다.
        List<Classifier.Recognition> txt = results.subList(0,1);
        Log.d("txt: ",txt.toString());
        Log.d("entire: ",results.toString());
//        BigImage.setImageBitmap(bitmap);
        detect.setText(results.toString());
        if(txt.toString().contains("female")){
            hashMap.put("gender","female");
        } else if(txt.toString().contains("male")){
            hashMap.put("gender","male");
        }
        reference.updateChildren(hashMap);


        hashMap.put("gender", detect.toString());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                    if(pot==1){
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                        reference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                    User user = snapshot.getValue(User.class);
                                    if (user.getId()!=null&&user.getId().equals(firebaseuser.getUid())) {
                                        myge = user.getGender();

                                    }

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        Thread.sleep(1000);
                        if(gender.equals(myge)){
                            Intent intent = new Intent(getApplicationContext(), PotchatActivity.class);
                            intent.putExtra("potname", title);
                            startActivity(intent);

                            finish();
                        }else {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "들어갈 수 없습니다.", Toast.LENGTH_SHORT).show();
                                }
                            });
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();
                        }


                    }else{
                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(i);
                        finish();
                    }
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }).start();

    }
}
