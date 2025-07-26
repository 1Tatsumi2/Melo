package com.example.musicapp.MusicManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.musicapp.Adapter.PlaylistAdapter;
import com.example.musicapp.Class.Playlist;
import com.example.musicapp.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AddPlaylistToMusicActivity extends AppCompatActivity {

    List<Playlist> playlistList;
    ListView lvSongs;
    SearchView searchView;
    PlaylistAdapter playlistAdapter;
    FirebaseAuth fAuth;
    String UserID;
    FirebaseFirestore db= FirebaseFirestore.getInstance();
    CollectionReference ref=db.collection("Playlist");
    String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_play_list_to_music);

        lvSongs = findViewById(R.id.lvPlaylistsToMusic);
        searchView = findViewById(R.id.searchPlaylistToMusic);
        Intent intent=getIntent();
        Bundle extraData=intent.getExtras();
        key=extraData.getString("key");
        playlistList = new ArrayList<>();
        fAuth=FirebaseAuth.getInstance();
        UserID=fAuth.getCurrentUser().getUid();
        playlistAdapter = new PlaylistAdapter(this, 3, playlistList);
        lvSongs.setAdapter(playlistAdapter);
        showAllPlaylist();


        ref.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(QueryDocumentSnapshot documentSnapshot:queryDocumentSnapshots)
                {
                    Playlist playlist=documentSnapshot.toObject(Playlist.class);
                    playlist.setKey(documentSnapshot.getId());
                    playlist.getAuthor().get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            String name=documentSnapshot.getId();
                            if(name.equals(UserID)&& Objects.equals(playlist.getClassified(), "My Playlist"))
                            {
                                playlistList.add(playlist);
                                playlistAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
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
                    showAllPlaylist();
                } else {
                    searchList(newText);
                }
                return true;
            }
        });

        lvSongs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Playlist playlist=playlistAdapter.getItem(position);
                String keys=playlist.getKey();
                DocumentReference songRef=db.collection("Music").document(key);
                DocumentReference playListRef=ref.document(keys);
                playListRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Map<String, Object> songs = (Map<String, Object>) documentSnapshot.get("songs");
                        if (songs != null && songs.containsKey(key)) {
                            // Nếu trường tồn tại, xóa nó
                            Map<String, Object> updates = new HashMap<>();
                            Double k=documentSnapshot.getDouble("songNumber");
                            Integer ik=k.intValue();
                            Integer updatedNumber=ik-1;
                            updates.put("songNumber",updatedNumber);
                            updates.put("songs." + key, FieldValue.delete());

                            playListRef.update(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(AddPlaylistToMusicActivity.this,"song removed",Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            // Nếu trường không tồn tại, thêm nó
                            Map<String, Object> updates = new HashMap<>();
                            Double k=documentSnapshot.getDouble("songNumber");
                            Integer ik=k.intValue();
                            Integer updatedNumber=ik+1;
                            updates.put("songNumber",updatedNumber);
                            updates.put("songs." + key, songRef);

                            playListRef.update(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(AddPlaylistToMusicActivity.this, "Added to playlist", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    private void searchList(String newText) {
        ArrayList<Playlist> searchList = new ArrayList<>();
        for (Playlist data : playlistList) {
            if(data.getName().toLowerCase().contains(newText.toLowerCase())) {
                searchList.add(data);
            }
        }
        playlistAdapter.searchPlaylist(searchList);
    }

    private void showAllPlaylist() {
        playlistAdapter.searchPlaylist((ArrayList<Playlist>) playlistList);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}