package com.example.musicapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.musicapp.Activities.SongsListActivity;
import com.example.musicapp.Activities.SongsListActivity;
import com.example.musicapp.Models.CategoryModel;
import com.example.musicapp.databinding.CategoryItemRecyclerRowBinding;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.MyViewHolder> {
    private List<CategoryModel> categoryList;

    public CategoryAdapter(List<CategoryModel> categoryList) {
        this.categoryList = categoryList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        CategoryItemRecyclerRowBinding binding = CategoryItemRecyclerRowBinding.inflate(layoutInflater, parent, false);
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        CategoryModel category = categoryList.get(position);
        holder.bindData(category);
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private CategoryItemRecyclerRowBinding binding;

        public MyViewHolder(CategoryItemRecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bindData(CategoryModel category) {
            binding.nameTextView.setText(category.getName());
            Glide.with(binding.coverImageView.getContext())
                    .load(category.getCoverUrl())
                    .into(binding.coverImageView);

            // Lấy context từ binding
            Context context = binding.getRoot().getContext();
            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Khởi động SongsListActivity
                    SongsListActivity.category = category;
                    Intent intent = new Intent(context, SongsListActivity.class);
                    context.startActivity(intent);
                }
            });

        }
    }
}
