package com.example.musicapp.Playlist;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.musicapp.MusicManager.MusicDetailActivity;
import com.example.musicapp.MusicManager.MusicManagerActivity;
import com.example.musicapp.Adapter.SongsAdapter;
import com.example.musicapp.Class.Song;
import com.example.musicapp.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddMusicToPlaylistActivity extends AppCompatActivity {

    List<Song> songArrayList;
    ListView lvSongs;
    SearchView searchView;
    SongsAdapter songsAdapter;
    FirebaseFirestore db=FirebaseFirestore.getInstance();
    String key;
    CollectionReference ref=db.collection("Songs");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_music_to_playlist);
        lvSongs = findViewById(R.id.lvSongsInPlayList);
        searchView = findViewById(R.id.searchMusicInPlaylist);
        Intent intent=getIntent();
        Bundle extraData=intent.getExtras();
        key=extraData.getString("keyPlaylist");
        songArrayList = new ArrayList<>();

        songsAdapter = new SongsAdapter(this, songArrayList);
        lvSongs.setAdapter(songsAdapter);
        showAllSongs();

        ref.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(QueryDocumentSnapshot documentSnapshot:queryDocumentSnapshots)
                {
                    Song song=documentSnapshot.toObject(Song.class);
                    song.setKey(documentSnapshot.getId());
                    songArrayList.add(song);
                    songsAdapter.notifyDataSetChanged();
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
                    showAllSongs();
                } else {
                    searchList(newText);
                }
                return true;
            }
        });

        lvSongs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Song song = songsAdapter.getItem(position);
                DocumentReference ref=db.collection("Playlist").document(key);
                DocumentReference musicRef=db.collection("Music").document(song.getKey());
                Map<String,Object> map=new HashMap<>();
                ref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Map<String, Object> songs = (Map<String, Object>) documentSnapshot.get("songs");
                        String uniqueKey=song.getKey();
                        if (songs != null && songs.containsKey(uniqueKey)) {
                            // Nếu trường tồn tại, xóa nó
                            Map<String, Object> updates = new HashMap<>();
                            Double k=documentSnapshot.getDouble("songNumber");
                            Integer ik=k.intValue();
                            Integer updatedNumber=ik-1;
                            updates.put("songNumber",updatedNumber);
                            updates.put("songs." + uniqueKey, FieldValue.delete());

                            ref.update(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(AddMusicToPlaylistActivity.this,"song removed",Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            // Nếu trường không tồn tại, thêm nó
                            Map<String, Object> updates = new HashMap<>();
                            Double k=documentSnapshot.getDouble("songNumber");
                            Integer ik=k.intValue();
                            Integer updatedNumber=ik+1;
                            updates.put("songNumber",updatedNumber);
                            updates.put("songs." + uniqueKey, musicRef);

                            ref.update(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(AddMusicToPlaylistActivity.this, "Added to playlist", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
            }
        });
    }
    public void searchList(String text) {
        ArrayList<Song> searchList = new ArrayList<>();
        for (Song data : songArrayList) {
            if(data.getNameSong().toLowerCase().contains(text.toLowerCase())) {
                searchList.add(data);
            }
        }
        songsAdapter.searchSongLst(searchList);
    }
    public void showAllSongs() {
        songsAdapter.searchSongLst((ArrayList<Song>) songArrayList);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}