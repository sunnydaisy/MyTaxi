package com.example.s1.mytaxi;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static com.google.android.gms.common.internal.safeparcel.SafeParcelable.NULL;

public class LocationActivity extends AppCompatActivity {
    TextView lat, lon, add, txt;
    Double latstr, lonstr;
    EditText editText;
    Button btn_a, btn_b, btn_fin;
    boolean mLocationPermissionGranted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        lat = findViewById(R.id.lat);
        lon = findViewById(R.id.lon);
        add = findViewById(R.id.add);
        btn_fin = findViewById(R.id.btn_fin);
        btn_b = findViewById(R.id.btn_b);
        btn_a = findViewById(R.id.btn_a);
        editText = findViewById(R.id.edittext);
        txt = findViewById(R.id.txt);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final LocationManager locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)){

            }
            else{
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},2002);
            }
        }
        if(getIntent().getIntExtra("location", 0)==2){
            getSupportActionBar().setTitle("도착지 입력");
            txt.setText("도착지 입력");
            btn_a.setVisibility(View.GONE);
            lat.setVisibility(View.GONE);
            lon.setVisibility(View.GONE);
        }else {
            getSupportActionBar().setTitle("출발지 입력");
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // and this
                startActivity(new Intent(LocationActivity.this, PotActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });


        btn_a.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationListener locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {


                        latstr = location.getLatitude();
                        lonstr = location.getLongitude();

                        lat.setText(latstr.toString());
                        lon.setText(lonstr.toString());

                        getLocation(latstr,lonstr);
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {


                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                    }
                };

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
            }
        });

        btn_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String txt_add = editText.getText().toString();
                if(TextUtils.isEmpty(txt_add))
                    Toast.makeText(LocationActivity.this, "빈칸입니다.", Toast.LENGTH_SHORT).show();
                add.setText(txt_add);
                lat.setVisibility(View.GONE);
                lon.setVisibility(View.GONE);
                lon.setText("1");
                lat.setText("1 ");
            }
        });

        btn_fin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("location", add.getText().toString());
                intent.putExtra("lon", lon.getText().toString());
                intent.putExtra("lat", lat.getText().toString());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    public void getLocation(Double lati, Double longi){

        List<Address> addresses = null;
        Geocoder geocoder = new Geocoder(this, Locale.KOREAN);
        try{
            addresses = geocoder.getFromLocation(lati,longi,1);
        } catch(IOException e){
            add.setText("위치로부터 주소를 인식할 수 없습니다. 다시 확인해주세요.");
            e.printStackTrace();
        }

        if(addresses.size()>0) {
            Address address = addresses.get(0);
            StringBuilder addressStringBuilder = new StringBuilder();

            Log.d("last index:",address.getMaxAddressLineIndex()+"");
            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressStringBuilder.append(address.getAddressLine(i));

            }
            String convadd = addressStringBuilder.toString();
            convadd = convadd.replace("대한민국 ","");


            add.setText(convadd);
        }


    }

    @Override
    public void onRequestPermissionsResult(int reqcode, @NonNull String perm[], @NonNull int[] grantres){

        mLocationPermissionGranted = false;
        switch (reqcode) {
            case 2002: {
                // If request is cancelled, the result arrays are empty.
                if (grantres.length > 0
                        && grantres[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
                else{

                }

            }
        }
    }

}
