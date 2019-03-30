package com.example.s1.mytaxi.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.s1.mytaxi.DetectActivity;
import com.example.s1.mytaxi.Model.Potlist;
import com.example.s1.mytaxi.Model.User;
import com.example.s1.mytaxi.Notifications.Data;
import com.example.s1.mytaxi.PotchatActivity;
import com.example.s1.mytaxi.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import static android.app.Activity.RESULT_OK;

public class PotAdapter extends RecyclerView.Adapter<PotAdapter.ViewHolder> {

    private Context mContext;
    private List<Potlist> mpot;
    FirebaseUser fuser;
    String myge, myname;
    String gender, title;
    private int REQUEST_POT = 1;
    public PotAdapter(Context mContext, List<Potlist> mpot) {
        this.mpot = mpot;
        this.mContext = mContext;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.pot_item, parent, false);
        return new PotAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        final Potlist pot = mpot.get(position);
        final String title = pot.getDest()+"("+pot.getDate()+")";
        holder.potname.setText(title);
        final String gender = pot.getMale();
        final String id = pot.getId();
        if(gender.equals(" "))
            holder.username.setText("출발지: "+pot.getDep());
        else
            holder.username.setText("출발지: "+pot.getDep()+", 동성만 탑승가능");
        holder.date.setText("date: "+pot.getTime());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    if (user.getId()!=null&&user.getId().equals(fuser.getUid())) {
                        myname = user.getUsername();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(gender.equals(" ")||id.equals(fuser.getUid())){
                    Intent intent = new Intent(mContext, PotchatActivity.class);
                    intent.putExtra("potname", title);
                    mContext.startActivity(intent);
                }else{
                    Toast.makeText(mContext, "얼굴인식 후 채팅방에 들어갈 수 있습니다.", Toast.LENGTH_SHORT).show();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Intent intent = new Intent(mContext, DetectActivity.class);
                    intent.putExtra("name", myname);
                    intent.putExtra("gender", gender);
                    intent.putExtra("title", title);
                    intent.putExtra("pot", REQUEST_POT);
                    mContext.startActivity(intent);

                }
            }
        });

    }



    @Override
    public int getItemCount() {
        return mpot.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView username;
        private TextView potname;
        private TextView date;

        public ViewHolder(View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            potname = itemView.findViewById(R.id.potname);
            date = itemView.findViewById(R.id.date);
        }
    }
}