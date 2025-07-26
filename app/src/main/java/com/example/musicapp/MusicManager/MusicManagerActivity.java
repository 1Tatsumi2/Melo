package com.example.musicapp.MusicManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.example.musicapp.UploadActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.musicapp.R;
import com.example.musicapp.Class.Song;
import com.example.musicapp.Adapter.SongsAdapter;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class MusicManagerActivity extends AppCompatActivity {

    private static final String TAG = "MusicManagerActivity";
    private ArrayList<Song> songArrayList;
    private ListView lvSongs;
    private SearchView searchView;
    private FloatingActionButton fab;
    private SongsAdapter songsAdapter;
    private FirebaseFirestore db;
    private CollectionReference songsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_manager);

        initializeViews();
        initializeFirestore();
        setupListView();
        fetchSongsFromFirestore();
        setupSearchView();
        setupFabListener();
        setupListViewItemClickListener();

        Log.d(TAG, "onCreate hoàn tất");
    }

    private void initializeViews() {
        lvSongs = findViewById(R.id.lvSongs);
        fab = findViewById(R.id.fab);
        searchView = findViewById(R.id.search);
        songArrayList = new ArrayList<>();
    }

    private void initializeFirestore() {
        db = FirebaseFirestore.getInstance();
        songsRef = db.collection("Songs");
    }

    private void setupListView() {
        songsAdapter = new SongsAdapter(this, songArrayList);
        lvSongs.setAdapter(songsAdapter);
    }

    private void fetchSongsFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Songs")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        songArrayList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Song song = document.toObject(Song.class);
                            songArrayList.add(song);
                            Log.d(TAG, "Đã thêm bài hát: " + song.getNameSong());
                        }
                        songsAdapter.notifyDataSetChanged();
                        Log.d(TAG, "Tổng số bài hát: " + songArrayList.size());
                    } else {
                        Log.e(TAG, "Lỗi khi lấy dữ liệu: ", task.getException());
                        showErrorToast("Không thể tải danh sách bài hát");
                    }
                });
    }
    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    songsAdapter.searchSongLst(songArrayList); // Hiển thị lại danh sách ban đầu
                } else {
                    searchList(newText);
                }
                return true;
            }
        });
    }

    private void setupFabListener() {
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MusicManagerActivity.this, UploadActivity.class);
            startActivity(intent);
        });
    }

    private void setupListViewItemClickListener() {
        lvSongs.setOnItemClickListener((adapterView, view, position, l) -> {
            Song song = songsAdapter.getItem(position);
            if (song != null) {
                Intent openMusicPlayer = new Intent(MusicManagerActivity.this, MusicDetailActivity.class);
                openMusicPlayer.putExtra("NameSong", song.getNameSong());
                openMusicPlayer.putExtra("Key", song.getKey());
                startActivity(openMusicPlayer);
            }
        });
    }

    private void searchList(String text) {
        ArrayList<Song> searchResults = new ArrayList<>();
        for (Song data : songArrayList) {
            if (data.getNameSong().toLowerCase().contains(text.toLowerCase()) ||
                    data.getSinger().toLowerCase().contains(text.toLowerCase())) {
                searchResults.add(data);
            }
        }
        songsAdapter.searchSongLst(searchResults);
    }

    private void showErrorToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchSongsFromFirestore();
    }
}
