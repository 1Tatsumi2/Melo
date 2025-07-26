package com.example.musicapp.Models;

public class SongModel {
    private String id;
    private String title;
    private String subtitle;
    private String url;
    private String coverUrl;

    // Constructor mặc định
    public SongModel() {
        this.id = "";
        this.title = "";
        this.subtitle = "";
        this.url = "";
        this.coverUrl = "";
    }

    // Constructor có tham số
    public SongModel(String id, String title, String subtitle, String url, String coverUrl) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.url = url;
        this.coverUrl = coverUrl;
    }

    // Getter và Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }
}
