package com.example.musicapp.Activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.musicapp.Adapters.SongsListAdapter;
import com.example.musicapp.Models.CategoryModel;
import com.example.musicapp.databinding.ActivitySongsListBinding;

public class SongsListActivity extends AppCompatActivity {

    public static CategoryModel category;

    private ActivitySongsListBinding binding;
    private SongsListAdapter songsListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySongsListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.nameTextView.setText(category.getName());

        Glide.with(binding.coverImageView.getContext())
                .load(category.getCoverUrl())
                .apply(new RequestOptions().transform(new RoundedCorners(32)))
                .into(binding.coverImageView);
        setupSongsListRecyclerView();
    }
    private void setupSongsListRecyclerView() {
        // Khởi tạo adapter với danh sách các bài hát
        songsListAdapter = new SongsListAdapter(category.getSongs(),this);
        // Thiết lập LayoutManager cho RecyclerView
        binding.songsListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Gán adapter cho RecyclerView
        binding.songsListRecyclerView.setAdapter(songsListAdapter);
    }

}
