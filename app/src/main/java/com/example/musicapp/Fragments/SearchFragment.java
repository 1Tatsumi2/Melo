package com.example.musicapp.Fragments;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.example.musicapp.Adapter.SongsAdapter;
import com.example.musicapp.Class.Song;
import com.example.musicapp.Activities.MusicPlayer;
import com.example.musicapp.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SearchFragment extends Fragment {

    List<Song> songArrayList;
    ListView lvSongs;
    SearchView searchView;

    SongsAdapter songsAdapter;
    String receivedString;
    Boolean isSth;
    FirebaseFirestore db=FirebaseFirestore.getInstance();
    CollectionReference ref=db.collection("Songs");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        lvSongs = view.findViewById(R.id.lvSongs);
        searchView=view.findViewById(R.id.search);
        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(Color.WHITE);  // Đặt màu chữ trắng
        searchEditText.setHintTextColor(Color.LTGRAY);  // Đặt màu gợi ý là xám nhạt

        songArrayList = new ArrayList<>();

        songsAdapter = new SongsAdapter(getActivity(), songArrayList);
        lvSongs.setAdapter(songsAdapter);
        Bundle arguments = getArguments();
        if (arguments != null) {
            receivedString = arguments.getString("search");
            isSth=arguments.getBoolean("isSth");
        }
        if (isSth==null)
        {
            isSth=false;
        }
        showAllSongs();
        ref.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(QueryDocumentSnapshot documentSnapshot:queryDocumentSnapshots)
                {
                    Song song=documentSnapshot.toObject(Song.class);
                    song.setKey(documentSnapshot.getId());
                    songArrayList.add(song);
                }
                songsAdapter.notifyDataSetChanged();
                if(isSth)
                {
                    searchView.setQuery(receivedString, false);
                    searchList(receivedString);
                    isSth=false;
                }
                else {
                    showAllSongs();
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
                int originalPosition = songArrayList.indexOf(song);
                Intent openMusicPlayer = new Intent(getActivity(), MusicPlayer.class);
                openMusicPlayer.putExtra("NameSong", song);
                openMusicPlayer.putExtra("MP3", (Serializable) songArrayList);
                openMusicPlayer.putExtra("Key",song.getKey());
                openMusicPlayer.putExtra("position",originalPosition);
                startActivity(openMusicPlayer);
            }
        });

        return view;


    }

    public void searchList(String text)
    {
        ArrayList<Song> searchList=new ArrayList<>();
        for(Song data:songArrayList)
        {
            if(data.getNameSong().toLowerCase().contains(text.toLowerCase()) ||
                    data.getSinger().toLowerCase().contains(text.toLowerCase()))
            {
                searchList.add(data);
            }
        }
        songsAdapter.searchSongLst(searchList);
    }
    public void showAllSongs() {
        songsAdapter.searchSongLst((ArrayList<Song>) songArrayList);
    }


}