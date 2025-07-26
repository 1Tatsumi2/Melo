package com.example.musicapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.musicapp.Class.Playlist;
import com.example.musicapp.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PlaylistAdapter extends ArrayAdapter<Playlist> {
    private List<Playlist> playlists;

    public PlaylistAdapter(@NonNull Context context, int resource, @NonNull List<Playlist> objects) {
        super(context, resource, objects);
        this.playlists = new ArrayList<>(objects);
    }

    @Nullable
    @Override
    public Playlist getItem(int position) {
        return playlists.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        convertView= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_playlist,null);
        TextView playlistTitle=convertView.findViewById(R.id.namePlaylist);
        TextView playlistAuthor=convertView.findViewById(R.id.authorPlaylist);
        TextView playlistNumSong=convertView.findViewById(R.id.songPlaylist);
        ImageView image=convertView.findViewById(R.id.imagePlaylist);
        Playlist playlist=getItem(position);

        Integer a=playlist.getSongNumber();
        String b=a.toString();

        playlistTitle.setText(playlist.getName());
        playlistNumSong.setText(b);


        playlist.getAuthor().get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(Objects.equals(documentSnapshot.getString("role"), "Admin") || Objects.equals(documentSnapshot.getString("role"), "Moderator"))
                {
                    playlistAuthor.setText("MusicApp");
                }
                else {
                    playlistAuthor.setText(documentSnapshot.getString("fName"));
                }
            }
        });
        Glide.with(getContext()).load(playlist.getImage()).into(image);
        return  convertView;
    }

    @Override
    public int getCount() {
        return playlists.size();
    }

    public void searchPlaylist(ArrayList<Playlist> searchList)
    {
        playlists = searchList;
        notifyDataSetChanged();
    }
}
