package com.example.musicapp.ManageUser;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.musicapp.Adapter.UserAdapter;
import com.example.musicapp.Class.Users;
import com.example.musicapp.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageUserActivity extends AppCompatActivity {

    List<Users> usersList;
    ListView lvUsers;
    SearchView searchView;
    UserAdapter userAdapter;
    FirebaseFirestore fStore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_user);

        lvUsers=findViewById(R.id.lvUsers);
        searchView=findViewById(R.id.searchUser);
        usersList=new ArrayList<>();
        userAdapter=new UserAdapter(this,1,usersList);
        lvUsers.setAdapter(userAdapter);
        showAllUser();
        fStore=FirebaseFirestore.getInstance();
        CollectionReference collection=fStore.collection("users");

        collection.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(QueryDocumentSnapshot documentSnapshot:queryDocumentSnapshots)
                {
                    Users users =documentSnapshot.toObject(Users.class);
                    users.setKey(documentSnapshot.getId());
                    usersList.add(users);
                }
                userAdapter.notifyDataSetChanged();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    showAllUser();
                } else {
                    searchList(newText);
                }
                return true;
            }
        });

        lvUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Users users = userAdapter.getItem(position);
                Intent i = new Intent(ManageUserActivity.this, UserDetailActivity.class);
                i.putExtra("user", users);
                i.putExtra("key",users.getKey());
                startActivity(i);
                finish();
            }
        });
    }

    public void searchList(String text) {
        ArrayList<Users> searchList = new ArrayList<>();
        for (Users data : usersList) {
            if(data.getfName().toLowerCase().contains(text.toLowerCase())){
                searchList.add(data);
            }
        }
        userAdapter.searchLst(searchList);
    }

    public void showAllUser() {
        userAdapter.searchLst((ArrayList<Users>) usersList);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}