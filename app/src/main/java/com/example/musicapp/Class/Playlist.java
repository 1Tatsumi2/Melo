package com.example.musicapp.Class;

import com.google.firebase.firestore.DocumentReference;

import java.io.Serializable;
import java.util.Map;

public class Playlist implements Serializable {
    private String name;
    private  String image;
    private String description;
    private Boolean isPublic;
    private DocumentReference author;
    private int songNumber;
    private Map<String, Object> songs;
    private  String classified;
    private  String key;

    public Playlist(String name, String image, String description, Boolean isPublic, DocumentReference author, int songNumber, String classified,Map<String,Object> songs) {
        this.name = name;
        this.image = image;
        this.description = description;
        this.isPublic = isPublic;
        this.author = author;
        this.songNumber = songNumber;
        this.classified=classified;
        this.songs = songs;
    }

    public Playlist() {
    }

    public Playlist(String name, String description, Boolean isPublic, DocumentReference author,String image,String classified) {
        this.name = name;
        this.description = description;
        this.isPublic = isPublic;
        this.author = author;
        this.image=image;
        this.classified=classified;
    }

    public Playlist(String name, String description, Boolean isPublic,String image,String classified) {
        this.name = name;
        this.description = description;
        this.isPublic = isPublic;
        this.image=image;
        this.classified=classified;
    }

    public String getClassified() {
        return classified;
    }

    public void setClassified(String classified) {
        this.classified = classified;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public DocumentReference getAuthor() {
        return author;
    }

    public void setAuthor(DocumentReference author) {
        this.author = author;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getPublic() {
        return isPublic;
    }

    public void setPublic(Boolean aPublic) {
        isPublic = aPublic;
    }

    public int getSongNumber() {
        return songNumber;
    }

    public void setSongNumber(int songNumber) {
        this.songNumber = songNumber;
    }

    public Map<String, Object> getSongs() {
        return songs;
    }

    public void setSongs(Map<String, Object> songs) {
        this.songs = songs;
    }
}

