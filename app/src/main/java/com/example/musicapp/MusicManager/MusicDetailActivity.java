package com.example.musicapp.MusicManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.musicapp.Class.Song;
import com.example.musicapp.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MusicDetailActivity extends AppCompatActivity {

    Bundle songExtraData;
    String key;
    TextView tvTime, tvTitle, tvArtist;
    TextView tvDuration;
    Button editBtn,deleteBtn;
    int position;
    ImageView tvImage;
    SeekBar seekBarTime;
    SeekBar seekBarVolume;
    static MediaPlayer mMediaPlayer;
    FirebaseFirestore db=FirebaseFirestore.getInstance();
    DocumentReference ref;
    Song song;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_music_detail);

        Intent intent = getIntent();
        songExtraData = intent.getExtras();
        key=songExtraData.getString("key");

        if (mMediaPlayer!=null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.reset();
        }

        // getting out the song name
        String name = song.getNameSong();
        tvTitle.setText(name);
        String artist=song.getSinger();
        String duration = millisecondsToString(song.getDuration());
        tvDuration.setText(duration);
        tvArtist.setText(artist);
        Uri uri = Uri.parse(song.getMP3());
        mMediaPlayer = MediaPlayer.create(this, uri);
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

                // seekbar
                seekBarTime.setMax(mMediaPlayer.getDuration());
                mMediaPlayer.start();
            }
        });

        mMediaPlayer.setLooping(true);
        seekBarVolume.setProgress(50);
        seekBarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean isFromUser) {
                // Handle progress change
                // You can use 'progress' to get the current progress value
                // tang giam am luong
                float volume = (float) progress / 100f;
                mMediaPlayer.setVolume(volume, volume);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Handle the start of tracking touch
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Handle the stop of tracking touch
            }
        });

        // thanh thoi gian bai hat
        seekBarTime.setMax(mMediaPlayer.getDuration());
        Handler handler = new Handler();
        // Update seek bar and time TextView periodically
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mMediaPlayer.isPlaying()) {
                    int currentPosition = mMediaPlayer.getCurrentPosition();
                    seekBarTime.setProgress(currentPosition);
                    tvTime.setText(millisecondsToString(currentPosition));
                }
                handler.postDelayed(this, 1000); // Update every 1 second
            }
        }, 0);
        seekBarTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mMediaPlayer.seekTo(progress);
                    tvTime.setText(millisecondsToString(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Handle the start of tracking touch
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Handle the stop of tracking touch
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                confirmDelete();
            }
        });
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaPlayer.stop();
                Intent intent=new Intent(MusicDetailActivity.this, UpdateActivity.class)
                        .putExtra("Title",song.getNameSong())
                        .putExtra("Artist",song.getSinger())
                        .putExtra("Album",song.getAlbum())
                        .putExtra("Image",song.getImage())
                        .putExtra("Audio",song.getMP3())
                        .putExtra("Key",song.getKey())
                        .putExtra("Duration",mMediaPlayer.getDuration());
                mMediaPlayer.stop();
                startActivity(intent);
                finish();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mMediaPlayer.stop();
        startActivity(new Intent(MusicDetailActivity.this, MusicManagerActivity.class));
        finish();
    }

    private void confirmDelete() {
        String[] options={"Yes","No"};
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Do you want to delete this song?");
        //set items/options
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handle click
                if(which==0)
                {
                    deleteSong();
                }
                else if(which==1)
                {
                    dialog.dismiss();
                }
            }
        });
        //create/show dialog
        builder.create().show();
    }

    private void deleteSong() {
        FirebaseStorage storage=FirebaseStorage.getInstance();
        StorageReference audioReference = storage.getReferenceFromUrl(song.getMP3());
        StorageReference imageReference = storage.getReferenceFromUrl(song.getImage());

        Task<Void> deleteAudio = audioReference.delete();
        Task<Void> deleteImage = imageReference.delete();

        deleteAudio.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return deleteImage;
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ref=db.collection("Music").document(key);
                //xóa file audio và image cũ(not done)
                ref.delete();
                Toast.makeText(MusicDetailActivity.this, "Delete success", Toast.LENGTH_SHORT).show();
                mMediaPlayer.stop();
                startActivity(new Intent(MusicDetailActivity.this, MusicManagerActivity.class));
                finish();
            }
        });
    }

    //chinh thoi gian
    public String millisecondsToString(int time) {
        int minutes = time / 1000 / 60;
        int seconds = time / 1000 % 60;
        return minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
    }
}