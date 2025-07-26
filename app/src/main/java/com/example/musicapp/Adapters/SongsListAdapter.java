package com.example.musicapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.musicapp.Activities.MusicPlayer;
import com.example.musicapp.Class.Song;
import com.example.musicapp.databinding.SongListItemRecylerRowBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class SongsListAdapter extends RecyclerView.Adapter<SongsListAdapter.MyViewHolder> {

    private final List<String> songIdList;
    private final Context context;  // Context cần để khởi động Activity

    // Constructor nhận danh sách songId và context
    public SongsListAdapter(List<String> songIdList, Context context) {
        this.songIdList = songIdList;
        this.context = context;
    }

    // ViewHolder class
    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private final SongListItemRecylerRowBinding binding;

        public MyViewHolder(SongListItemRecylerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        // Bind dữ liệu với view
        public void bindData(String songId, Context context) {
            FirebaseFirestore.getInstance().collection("Songs")
                    .document(songId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        Song song = documentSnapshot.toObject(Song.class);
                        if (song != null) {
                            binding.songTitleTextView.setText(song.getNameSong());
                            binding.songSubtitleTextView.setText(song.getSinger());
                            Glide.with(binding.songCoverImageView.getContext())
                                    .load(song.getImage())
                                    .apply(new RequestOptions().transform(new RoundedCorners(32)))
                                    .into(binding.songCoverImageView);

                            // Xử lý khi người dùng nhấp vào một bài hát
                            binding.getRoot().setOnClickListener(v -> {
                                // Tạo Intent để mở MusicPlayer và truyền dữ liệu
                                Intent intent = new Intent(context, MusicPlayer.class);
                                intent.putExtra("songId", songId);
                                intent.putExtra("songTitle", song.getNameSong());
                                intent.putExtra("songArtist", song.getSinger());
                                intent.putExtra("songImage", song.getImage());  // Có thể dùng trong MusicPlayer để hiển thị ảnh bìa
                                context.startActivity(intent);  // Khởi động MusicPlayer activity
                            });
                        }
                    });
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SongListItemRecylerRowBinding binding = SongListItemRecylerRowBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.bindData(songIdList.get(position), context);  // Truyền context và songId vào ViewHolder
    }

    @Override
    public int getItemCount() {
        return songIdList.size();
    }
}
