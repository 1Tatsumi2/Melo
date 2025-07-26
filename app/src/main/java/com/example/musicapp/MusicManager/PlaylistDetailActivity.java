package com.example.musicapp.MusicManager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.musicapp.Activities.MusicPlayer;
import com.example.musicapp.Adapter.SongsAdapter;
import com.example.musicapp.Class.Playlist;
import com.example.musicapp.Class.Song;
import com.example.musicapp.Playlist.EditPlaylistActivity;
import com.example.musicapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PlaylistDetailActivity extends AppCompatActivity {

    TextView numSong,name,desc,author;
    ImageView image;
    Button addMusic,deleteMusic,editPlaylist;
    String key,imageToDelete;
    List<Song> songArrayList;
    ListView lvSongs;
    SongsAdapter songsAdapter;
    FirebaseAuth fAuth;
    String UserID,ID,role,rolePlay;
    Playlist playlist;
    FirebaseFirestore db= FirebaseFirestore.getInstance();
    DocumentReference ref;
    CollectionReference refUser=db.collection("users");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_playlist_detail);
        numSong=findViewById(R.id.detailPlaylistNumSong);
        name=findViewById(R.id.detailPLaylistName);
        author=findViewById(R.id.detailPLaylistAuthor);
        desc=findViewById(R.id.detailPlaylistDesc);
        image=findViewById(R.id.detailPlaylistImage);
        deleteMusic=findViewById(R.id.DeletePlaylist);
        editPlaylist=findViewById(R.id.EditPlaylist);
        lvSongs = findViewById(R.id.lvPlaylistSong);
        addMusic=findViewById(R.id.addMusicToPlaylist);
        songArrayList = new ArrayList<>();
        songsAdapter = new SongsAdapter(this, songArrayList);
        lvSongs.setAdapter(songsAdapter);
        fAuth=FirebaseAuth.getInstance();
        UserID=fAuth.getCurrentUser().getUid();
        showAllSongs();
        Intent intent=getIntent();
        Bundle extraData=intent.getExtras();
        key=extraData.getString("key");
        imageToDelete=extraData.getString("playlist");
        DocumentReference UserRef=refUser.document(UserID);
        DocumentReference ref=db.collection("Playlist").document(key);
        ref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {


            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                playlist = documentSnapshot.toObject(Playlist.class);
                documentSnapshot.getDocumentReference("author").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (Objects.equals(documentSnapshot.getString("role"), "Admin") || Objects.equals(documentSnapshot.getString("role"), "Moderator")) {
                            author.setText("DoAnChill");
                        } else {
                            author.setText(documentSnapshot.getString("fName"));
                        }
                        rolePlay = documentSnapshot.getString("role");
                        ID = documentSnapshot.getId();
                        if (!Objects.equals(UserID, ID)) {
                            addMusic.setVisibility(View.GONE);
                            editPlaylist.setVisibility(View.GONE);
                            deleteMusic.setVisibility(View.GONE);
                            UserRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    role = documentSnapshot.getString("role");
                                    if ((Objects.equals(rolePlay, "Admin") || Objects.equals(rolePlay, "Moderator")) && (Objects.equals(role, "Admin") || Objects.equals(role, "Moderator"))) {
                                        addMusic.setVisibility(View.VISIBLE);
                                        editPlaylist.setVisibility(View.VISIBLE);
                                        deleteMusic.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                        }
                    }
                });
                name.setText(documentSnapshot.getString("name"));
                desc.setText(documentSnapshot.getString("description"));
                Double k = documentSnapshot.getDouble("songNumber");
                Integer ik = k.intValue();
                String tk = ik.toString();
                numSong.setText(tk + " songs");
                Glide.with(PlaylistDetailActivity.this).load(documentSnapshot.getString("image")).into(image);
                Map<String, Object> map = (Map<String, Object>) documentSnapshot.get("songs");
                if (map != null) {
                    for (Map.Entry<String, Object> entry : map.entrySet()) {
                        DocumentReference reference = (DocumentReference) entry.getValue();
                        reference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    Song song = documentSnapshot.toObject(Song.class);
                                    song.setKey(documentSnapshot.getId());
                                    songArrayList.add(song);
                                    songsAdapter.notifyDataSetChanged();
                                } else {
                                    ref.update("songs." + entry.getKey(), FieldValue.delete());
                                }
                            }
                        });
                    }
                }


                addMusic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(PlaylistDetailActivity.this, AddPlaylistToMusicActivity.class);
                        i.putExtra("keyPlaylist", key);
                        startActivity(i);
                        finish();
                    }
                });

                deleteMusic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        confirmDelete(PlaylistDetailActivity.this);
                    }
                });
                editPlaylist.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(PlaylistDetailActivity.this, EditPlaylistActivity.class);
                        i.putExtra("name", playlist.getName());
                        i.putExtra("description", playlist.getDescription());
                        i.putExtra("public", playlist.getPublic());
                        i.putExtra("classified", playlist.getClassified());
                        i.putExtra("image", playlist.getImage());
                        i.putExtra("key", key);
                        startActivity(i);
                        finish();
                    }
                });
                lvSongs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        Song song = songArrayList.get(position);
                        Intent openMusicPlayer = new Intent(PlaylistDetailActivity.this, MusicPlayer.class);
                        openMusicPlayer.putExtra("song", song);
                        openMusicPlayer.putExtra("key", song.getKey());
                        openMusicPlayer.putExtra("musics", (Serializable) songArrayList);
                        openMusicPlayer.putExtra("position", position);
                        startActivity(openMusicPlayer);
                        finish();
                    }
                });


            }

            private void confirmDelete(Context context) {
                String[] options = {"Yes", "No"};
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Do you want to delete this playlist?");
                //set items/options
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle click
                        if (which == 0) {
                            deleteSong();
                        } else if (which == 1) {
                            dialog.dismiss();
                        }
                    }
                });
                //create/show dialog
                builder.create().show();
            }

            private void deleteSong() {
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference imageReference = storage.getReferenceFromUrl(imageToDelete);

                // Xóa hình ảnh từ Firebase Storage
                imageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Xóa tài liệu Playlist sau khi hình ảnh đã được xóa thành công
                        final DocumentReference ref = db.collection("Playlist").document(key);
                        ref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(PlaylistDetailActivity.this, "Delete success", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(PlaylistDetailActivity.this, "Failed to delete playlist", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PlaylistDetailActivity.this, "Failed to delete image", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        });

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void showAllSongs() {
        songsAdapter.searchSongLst((ArrayList<Song>) songArrayList);
    }
}