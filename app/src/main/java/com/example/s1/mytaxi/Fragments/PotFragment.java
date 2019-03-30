package com.example.s1.mytaxi.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.s1.mytaxi.Adapter.PotAdapter;
import com.example.s1.mytaxi.Model.Potlist;
import com.example.s1.mytaxi.PotActivity;
import com.example.s1.mytaxi.R;
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
import java.util.List;

public class PotFragment extends Fragment {

    private RecyclerView recyclerView;
    private PotAdapter potadapter;
    private EditText editText;
    private EditText edit_date;
    private Button btn_pot;
    private Button btn_now;
    private Button btn_lct;
    private Button btn_date;

    private List<Potlist> mpot;

    FirebaseUser fuser;
    DatabaseReference reference;
    boolean mLocationPermissionGranted;
    ProgressBar loadingProgress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_pot, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        editText = (EditText) view.findViewById(R.id.edit_lct);
        edit_date = (EditText) view.findViewById(R.id.edit_date);
        btn_date = (Button) view.findViewById(R.id.btn_date);
        btn_pot = (Button) view.findViewById(R.id.btn_pot);
        btn_now = (Button) view.findViewById(R.id.btn_now);
        btn_lct = (Button) view.findViewById(R.id.btn_lct);
        loadingProgress = (ProgressBar) view.findViewById(R.id.regProgressBar);

        final LocationManager locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        if(ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)){

            }
            else{
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},2002);
            }
        }

        mpot = new ArrayList<>();
        readList();

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Potlist");
        btn_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingProgress.setVisibility(View.VISIBLE);

                LocationListener locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        final Double myLatnow,myLonnow;
                        myLatnow = location.getLatitude();
                        myLonnow = location.getLongitude();

                        reference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                mpot.clear();

                                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                    Potlist pot = snapshot.getValue(Potlist.class);
                                    Log.d("inside snapshot",pot.getLat());
                                    Double othersLat = Double.valueOf(pot.getLat());
                                    Double othersLon = Double.valueOf(pot.getLon());
                                    Double result = calcDistance(myLatnow,myLonnow,othersLat,othersLon);
                                    if(result < 500 ) {
                                        mpot.add(pot);
                                    }

                                }

                                potadapter = new PotAdapter(getContext(), mpot);
                                recyclerView.setAdapter(potadapter);
                                loadingProgress.setVisibility(View.INVISIBLE);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

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
        btn_lct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String loc = editText.getText().toString();
                final String date = edit_date.getText().toString();

                if(TextUtils.isEmpty(loc)&TextUtils.isEmpty(date)){
                    readList();
                }else{
                    readList(loc, date);
                }

            }
        });

        btn_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String date = edit_date.getText().toString();
                final String loc = editText.getText().toString();
                if(TextUtils.isEmpty(date)&&TextUtils.isEmpty(loc)){
                    readList();
                }else{
                    readList(loc, date);
                }
            }
        });

        btn_pot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), PotActivity.class);
                startActivity(intent);
            }
        });

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                readList();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return view;
    }


    private void readList() {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Potlist");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mpot.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Potlist pot = snapshot.getValue(Potlist.class);
                    mpot.add(pot);
                }

                potadapter = new PotAdapter(getContext(), mpot);
                recyclerView.setAdapter(potadapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


        private void readList(String l, String d) {
        final String dest= l;
        final String date= d;
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Potlist");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mpot.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Potlist pot = snapshot.getValue(Potlist.class);
                    if(TextUtils.isEmpty(date)){
                        if(pot.getDest().equals(dest))
                            mpot.add(pot);
                    }else if(TextUtils.isEmpty(dest)){
                        if(pot.getDate().equals(date))
                            mpot.add(pot);
                    }else{
                        if(pot.getDest().equals(dest)&&pot.getDate().equals(date))
                            mpot.add(pot);
                    }
                }

                potadapter = new PotAdapter(getContext(), mpot);
                recyclerView.setAdapter(potadapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private double calcDistance(Double lat1, Double lon1, Double lat2, Double lon2){

        double theta, dist;
        theta = lon1 - lon2;
        dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);

        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;    // 단위 mile 에서 km 변환.
        dist = dist * 1000.0;      // 단위  km 에서 m 로 변환

        return dist;
    }

    // 주어진 도(degree) 값을 라디언으로 변환
    private double deg2rad(double deg){
        return (double)(deg * Math.PI / (double)180d);
    }

    // 주어진 라디언(radian) 값을 도(degree) 값으로 변환
    private double rad2deg(double rad){
        return (double)(rad * (double)180d / Math.PI);
    }
}