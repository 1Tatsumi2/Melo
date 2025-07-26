package com.example.musicapp.Class;

import java.io.Serializable;

public class Song implements Serializable {

    private String Album;
    private int Duration;
    private String Image;
    private String Key;
    private String MP3;
    private String NameSong;
    private String Singer;
    private String Video;

    // Constructor mặc định (cần thiết cho Firebase)
    public Song(String nameUpdate, String singerUpdate, String audioUrl, int duration, String imageUrl, String albumUpdate, String videoUpdate) {}
    public  Song() {}
    // Constructor đầy đủ
    public Song(String Album, int Duration, String Image, String Key, String MP3, String NameSong, String Singer, String Video) {
        this.Album = Album;
        this.Duration = Duration;
        this.Image = Image;
        this.Key = Key;
        this.MP3 = MP3;
        this.NameSong = NameSong;
        this.Singer = Singer;
        this.Video = Video;
    }

    // Getter và Setter
    public String getAlbum() {
        return Album;
    }

    public void setAlbum(String Album) {
        this.Album = Album;
    }

    public int getDuration() {
        return Duration;
    }

    public void setDuration(int Duration) {
        this.Duration = Duration;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String Image) {
        this.Image = Image;
    }

    public String getKey() {
        return Key;
    }

    public void setKey(String Key) {
        this.Key = Key;
    }

    public String getMP3() {
        return MP3;
    }

    public void setMP3(String MP3) {
        this.MP3 = MP3;
    }

    public String getNameSong() {
        return NameSong;
    }

    public void setNameSong(String NameSong) {
        this.NameSong = NameSong;
    }

    public String getSinger() {
        return Singer;
    }

    public void setSinger(String Singer) {
        this.Singer = Singer;
    }

    public String getVideo()     {
        return Video;
    }

    public void setVideo(String Video) {
        this.Video = Video;
    }
}

