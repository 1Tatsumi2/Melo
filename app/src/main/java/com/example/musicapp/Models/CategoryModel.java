package com.example.musicapp.Models;

import java.util.List;
import java.util.Objects;

public class CategoryModel {
    private String name;
    private String coverUrl;
    private List<String> Songs;  // Thêm danh sách các bài hát

    // Constructor với tham số
    public CategoryModel(String name, String coverUrl, List<String> songs) {
        this.name = name;
        this.coverUrl = coverUrl;
        this.Songs = songs;
    }

    // Constructor mặc định
    public CategoryModel() {
        this.name = "";
        this.coverUrl = "";
        this.Songs = null;  // Khởi tạo danh sách rỗng
    }

    // Getters và Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public List<String> getSongs() {
        return Songs;
    }

    public void setSongs(List<String> songs) {
        this.Songs = songs;
    }

    // Override phương thức toString()
    @Override
    public String toString() {
        return "CategoryModel{" +
                "name='" + name + '\'' +
                ", coverUrl='" + coverUrl + '\'' +
                ", songs=" + Songs +
                '}';
    }

    // Override phương thức equals()
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoryModel that = (CategoryModel) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(coverUrl, that.coverUrl) &&
                Objects.equals(Songs, that.Songs);  // So sánh cả danh sách bài hát
    }

    // Override phương thức hashCode()
    @Override
    public int hashCode() {
        return Objects.hash(name, coverUrl, Songs);  // Bao gồm cả danh sách bài hát
    }
}
