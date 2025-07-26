package com.example.musicapp.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.musicapp.Adapters.CategoryAdapter;
import com.example.musicapp.Models.CategoryModel;
import com.example.musicapp.databinding.FragmentHomeBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class HomeFragment extends Fragment {

    // View Binding
    private FragmentHomeBinding binding;
    private CategoryAdapter categoryAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Khởi tạo View Binding
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        // Gọi hàm để lấy dữ liệu từ Firestore
        getCategories();

        // Trả về view đã được binding
        return binding.getRoot();
    }

    // Lấy danh sách category từ Firestore
    private void getCategories() {
        FirebaseFirestore.getInstance().collection("category")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    // Chuyển dữ liệu từ Firestore thành danh sách CategoryModel
                    List<CategoryModel> categoryList = querySnapshot.toObjects(CategoryModel.class);

                    // Thiết lập RecyclerView để hiển thị dữ liệu
                    setupCategoryRecyclerView(categoryList);
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace(); // Xử lý lỗi nếu có
                });
    }

    // Thiết lập RecyclerView với danh sách category
    private void setupCategoryRecyclerView(List<CategoryModel> categoryList) {
        // Kiểm tra nếu binding không phải là null trước khi sử dụng
        if (binding != null) {
            // Khởi tạo adapter
            categoryAdapter = new CategoryAdapter(categoryList);

            // Sử dụng LinearLayoutManager với chiều ngang
            binding.categoriesRecyclerView.setLayoutManager(
                    new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
            );

            // Gán adapter cho RecyclerView
            binding.categoriesRecyclerView.setAdapter(categoryAdapter);
        } else {
            throw new IllegalStateException("Binding is null. Ensure that onCreateView() is called before attempting to set up RecyclerView.");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Đặt binding thành null khi view bị hủy để tránh rò rỉ bộ nhớ
        binding = null;
    }
}
