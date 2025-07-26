package com.example.musicapp.ManageUser;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.musicapp.Class.Users;
import com.example.musicapp.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UserDetailActivity extends AppCompatActivity {

    Button changeRole,changePremium;
    TextView detailName, detailEmail, detailRole,yesNo;
    ImageView detailImage;
    FirebaseFirestore fStore;
    FirebaseAuth fAuth;
    String key;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_detail);
        detailEmail=findViewById(R.id.detailEmail);
        detailName=findViewById(R.id.DetailName);
        detailRole=findViewById(R.id.detailRole);
        detailImage=findViewById(R.id.detailUserImage);
        changeRole=findViewById(R.id.changeRole);
        changePremium=findViewById(R.id.changePremium);
        yesNo=findViewById(R.id.yesNo);
        fAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();
        Intent intent=getIntent();
        Bundle extraData=intent.getExtras();
        Users users1=(Users) extraData.getSerializable("user");
        key=extraData.getString("key");
        if(users1.getPremium())
        {
            yesNo.setText("Active");
        }
        else {
            yesNo.setText("No");
        }

        detailName.setText(users1.getfName());
        detailEmail.setText(users1.getEmail());
        detailRole.setText(users1.getRole());
        Glide.with(this).load(users1.getImage()).into(detailImage);

        if(Objects.equals(users1.getRole(), "Admin"))
        {
            changeRole.setVisibility(View.GONE);
            changePremium.setVisibility(View.GONE);
        }
        DocumentReference currentRef=fStore.collection("users").document(fAuth.getCurrentUser().getUid());

        currentRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(Objects.equals(documentSnapshot.getString("role"), "Moderator") && Objects.equals(users1.getRole(), "Moderator"))
                {
                    changeRole.setVisibility(View.GONE);
                    changePremium.setVisibility(View.GONE);
                }
            }
        });

        changeRole.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Objects.equals(users1.getRole(), "Moderator"))
                {
                    DocumentReference documentReference=fStore.collection("users").document(key);
                    Map<String,Object> edited=new HashMap<>();
                    edited.put("email",users1.getEmail());
                    edited.put("fName",users1.getfName());
                    edited.put("image",users1.getImage());
                    edited.put("premium",users1.getPremium());
                    edited.put("role","User");
                    documentReference.update(edited).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(UserDetailActivity.this, "Set User", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(UserDetailActivity.this, ManageUserActivity.class));
                            finish();
                        }
                    });
                }
                if(Objects.equals(users1.getRole(), "User")) {
                    DocumentReference documentReference=fStore.collection("users").document(key);
                    Map<String,Object> edited=new HashMap<>();
                    edited.put("email",users1.getEmail());
                    edited.put("fName",users1.getfName());
                    edited.put("image",users1.getImage());
                    edited.put("premium",true);
                    edited.put("role","Moderator");
                    documentReference.update(edited).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(UserDetailActivity.this, "Set Moderator", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(UserDetailActivity.this, ManageUserActivity.class));
                            finish();
                        }
                    });
                }
            }
        });

        changePremium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(users1.getPremium())
                {
                    DocumentReference documentReference=fStore.collection("users").document(key);
                    Map<String,Object> edited=new HashMap<>();
                    edited.put("email",users1.getEmail());
                    edited.put("fName",users1.getfName());
                    edited.put("image",users1.getImage());
                    edited.put("premium",false);
                    edited.put("role",users1.getRole());
                    documentReference.update(edited).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(UserDetailActivity.this, "Delete Premium", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(UserDetailActivity.this, ManageUserActivity.class));
                            finish();
                        }
                    });
                }
                else {
                    DocumentReference documentReference=fStore.collection("users").document(key);
                    Map<String,Object> edited=new HashMap<>();
                    edited.put("email",users1.getEmail());
                    edited.put("fName",users1.getfName());
                    edited.put("image",users1.getImage());
                    edited.put("premium",true);
                    edited.put("role",users1.getRole());
                    documentReference.update(edited).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(UserDetailActivity.this, "SetPremium", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(UserDetailActivity.this, ManageUserActivity.class));
                            finish();
                        }
                    });
                }
            }
        });
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(UserDetailActivity.this, ManageUserActivity.class));
        finish();
    }
}