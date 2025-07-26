package com.example.musicapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.musicapp.R;
import com.example.musicapp.Class.Song;

import java.util.ArrayList;
import java.util.List;

public class SongsAdapter extends ArrayAdapter<Song> {
    private Context context;
    private List<Song> songList;

    // Constructor
    public SongsAdapter(Context context, List<Song> songList) {
        super(context, 0, songList);
        this.context = context;
        this.songList = songList;
    }

    @Nullable
    @Override
    public Song getItem(int position) {
        return songList.get(position);  // Use the correct list
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false);
        }

        Song currentSong = songList.get(position);

        TextView tvNameSong = convertView.findViewById(R.id.tvNameSong);
        TextView tvSinger = convertView.findViewById(R.id.tvSinger);

        // Gán giá trị cho các TextView
        tvNameSong.setText(currentSong.getNameSong());
        tvSinger.setText(currentSong.getSinger());

        return convertView;
    }

    public String millisecondsToString(int time) {
        int minutes = time / 1000 / 60;
        int seconds = time / 1000 % 60;
        return minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
    }

    @Override
    public int getCount() {
        return songList != null ? songList.size() : 0;  // Check for null to avoid NullPointerException
    }

    // Method to search and update the song list
    public void searchSongLst(ArrayList<Song> searchList) {
        songList = searchList;
        notifyDataSetChanged();  // Notify adapter to refresh the list
    }
}

