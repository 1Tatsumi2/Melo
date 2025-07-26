package com.example.musicapp.Activities;

import static com.example.musicapp.ApplicationClass.CHANNEL_ID_2;
import static com.example.musicapp.ApplicationClass.ACTION_NEXTS;
import static com.example.musicapp.ApplicationClass.ACTION_PLAY;
import static com.example.musicapp.ApplicationClass.ACTION_PREV;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.tv.AdRequest;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.example.musicapp.Interface.ActionPlaying;
import com.example.musicapp.Interface.MusicService;
import com.example.musicapp.Interface.NotificationReceiver;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.musicapp.Class.Song;
import com.example.musicapp.Interface.MusicService;
import com.example.musicapp.Interface.NotificationReceiver;
import com.example.musicapp.MusicManager.AddPlaylistToMusicActivity;
import com.example.musicapp.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.glide.transformations.BlurTransformation;

public class MusicPlayer extends AppCompatActivity implements ActionPlaying, ServiceConnection {

    Bundle songExtraData;
    TextView tvTime, tvTitle, tvArtist;
    TextView tvDuration;
    int position,currentPos;
    ImageView nextBtn, previousBtn,back, btnShuffle,btnLoop;
    CircleImageView tvImage;
    SeekBar seekBarTime;
    SeekBar seekBarVolume;
    Button btnPlay;
    String key;
    ImageButton dotbutton;
    static MediaPlayer mMediaPlayer;
    ArrayList<Song> musicList;

    NotificationManager notificationManager;
    MusicService musicService;
    MediaSessionCompat mediaSession;
//    AdView mAdView;
    ObjectAnimator objectAnimator;
    LinearLayout layout;
    String currentImageUri,UserID;
    FirebaseFirestore db= FirebaseFirestore.getInstance();
    FirebaseAuth fAuth= FirebaseAuth.getInstance();

    Song song;
    private boolean isShuffleOn = false;
    private boolean isLoopOn = false;
    private ArrayList<Integer> playedSongs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        UserID=firebaseAuth.getCurrentUser().getUid();
        DocumentReference userReff=db.collection("Account").document(UserID);
//        userReff.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//            @Override
//            public void onSuccess(DocumentSnapshot documentSnapshot) {
//                if(!documentSnapshot.getBoolean("premium"))
//                {
//                    MobileAds.initialize(MusicPlayerActivity.this, new OnInitializationCompleteListener() {
//                        @Override
//                        public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
//
//                        }
//                    });
//                    mAdView = findViewById(R.id.adbanner);
//                    AdRequest adRequest = new AdRequest.Builder().build();
//                    mAdView.loadAd(adRequest);
//                }
//            }
//        });


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Nhận dữ liệu từ Intent
        song = (Song) getIntent().getSerializableExtra("song");
        btnShuffle = findViewById(R.id.shuffleBtn);
        btnLoop=findViewById(R.id.LoopBtn);
        tvTime = findViewById(R.id.tvTime);
        tvImage=findViewById(R.id.tvImage);
        tvDuration = findViewById(R.id.tvDuration);
        seekBarTime = findViewById(R.id.seekBarTime);
        seekBarVolume = findViewById(R.id.seekBarVolume);
        btnPlay = findViewById(R.id.btnPlay);
        nextBtn = findViewById(R.id.next);
        previousBtn = findViewById(R.id.previous);
        tvTitle = findViewById(R.id.tvTitle);
        tvArtist = findViewById(R.id.tvArtist);
        back = findViewById(R.id.btnBack);
        dotbutton = findViewById(R.id.dotButton);
        mediaSession=new MediaSessionCompat(this,"PlayerAudio");
        objectAnimator = ObjectAnimator.ofFloat(tvImage,"rotation",0f,360f);
        objectAnimator.setDuration(30000);
        objectAnimator.setRepeatCount(ValueAnimator.INFINITE);
        objectAnimator.setRepeatMode(ValueAnimator.RESTART);
        objectAnimator.setInterpolator(new LinearInterpolator());
        objectAnimator.start();
        UserID=fAuth.getCurrentUser().getUid();
        layout = findViewById(R.id.layout);
        musicList = new ArrayList<>();



        if(mMediaPlayer!=null)
        {
            mMediaPlayer.stop();
        }

        //getting values from previous activity
        Intent intent = getIntent();
        songExtraData = intent.getExtras();
        if (songExtraData != null) {
            key = songExtraData.getString("Key");
            musicList = (ArrayList)songExtraData.getParcelableArrayList("musicList");
            position = songExtraData.getInt("position", 0);
            currentPos = position;

            if (musicList != null && !musicList.isEmpty()) {
                initializeMusicPlayer(position);
            } else {
                // Xử lý khi musicList bị null hoặc rỗng
                Log.e("MusicPlayer", "musicList is null or empty");
            }
        } else {
            // Xử lý khi songExtraData bị null
            Log.e("MusicPlayer", "songExtraData is null");
        }

        btnShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isShuffleOn = !isShuffleOn;
                if (isShuffleOn) {
                    btnShuffle.setColorFilter(Color.parseColor("#4B1F9A")); // Màu tím
                } else {
                    btnShuffle.setColorFilter(Color.parseColor("#FFFFFFFF")); // Màu trắng
                }
            }
        });


        btnLoop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLoopOn = !isLoopOn;
                if (isLoopOn) {
                    btnLoop.setColorFilter(Color.parseColor("#4B1F9A")); // Màu tím
                } else {
                    btnLoop.setColorFilter(Color.parseColor("#FFFFFFFF")); // Màu trắng
                }
            }
        });
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playClicked();
            }
        });
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextClicked();
            }
        });
        previousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prevClicked();
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaPlayer.stop();
                finish();
            }
        });

    } //end main

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void initializeMusicPlayer(int position) {
        // if mediaplayer is not null and playing reset it at the launch of activity

        if (mMediaPlayer!=null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.reset();
        }
        currentPos=position;
        // getting out the song name
        String name = musicList.get(position).getNameSong();
        tvTitle.setText(name);
        String singer=musicList.get(position).getSinger();
        tvArtist.setText(singer);
        Glide.with(this)
                .asBitmap()
                .load(musicList.get(position).getImage())
                .apply(new RequestOptions().transform(new BlurTransformation(75))) // Sử dụng BlurTransformation để làm mờ hình ảnh
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        // Chuyển đổi Bitmap thành Drawable
                        Drawable drawable = new BitmapDrawable(getResources(), resource);

                        // Đặt Drawable làm hình nền cho View
                        layout.setBackground(drawable);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });
        String duration = millisecondsToString(musicList.get(position).getDuration());
        Glide.with(this).load(musicList.get(position).getImage()).into(tvImage);
        tvDuration.setText(duration);
        // accessing the songs on storage

        Uri uri = Uri.parse(musicList.get(position).getMP3());

        // creating a mediaplayer
        // passing the uri

        mMediaPlayer = MediaPlayer.create(this, uri);

        // SETTING ON PREPARED MEDIAPLAYER

        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

                // seekbar
                seekBarTime.setMax(mMediaPlayer.getDuration());

                // while mediaplayer is playing the play button should display pause
                btnPlay.setBackgroundResource(R.drawable.ic_pause);
                // start the mediaplayer
                showNotification(R.drawable.ic_pause,0F);
                mMediaPlayer.start();
            }
        });

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                btnPlay.setBackgroundResource(R.drawable.ic_play);
            }
        });

        //volume bar
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

        // if you want the the mediaplayer to go to next song after its finished playing one song its optional
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if(isLoopOn && isShuffleOn)
                {
                    btnPlay.setBackgroundResource(R.drawable.ic_play);
                    initializeMusicPlayer(position);
                }
                else if(isLoopOn)
                {
                    btnPlay.setBackgroundResource(R.drawable.ic_play);
                    initializeMusicPlayer(position);
                }
                else if(isShuffleOn) {
                    Random rand = new Random();
                    int newPosition = rand.nextInt(musicList.size());
                    currentPos=newPosition;
                    initializeMusicPlayer(newPosition);
                }
                else {
                    btnPlay.setBackgroundResource(R.drawable.ic_play);
                    int currentPosition = position;
                    if (currentPosition < musicList.size() -1) {
                        currentPosition++;
                    } else {
                        currentPosition = 0;
                    }
                    currentPos=currentPosition;
                    initializeMusicPlayer(currentPosition);
                }

            }
        });

        // working on seekbar
        dotbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(v.getContext(), dotbutton);
                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.dot_menu_button, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        // Handle menu item click here
                        if(item.getItemId()==R.id.add_to_playlist)
                        {
                            Intent i=new Intent(MusicPlayer.this, AddPlaylistToMusicActivity.class);
                            i.putExtra("Key",musicList.get(currentPos).getKey());
                            startActivity(i);
                            finish();
                        } else if (item.getItemId()==R.id.add_to_library) {
                            //pending
                            DocumentReference musicRef=db.collection("Songs").document(musicList.get(currentPos).getKey());
//                            DocumentReference refLib=db.collection("library").document(UserID);
//                            refLib.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                                @Override
//                                public void onSuccess(DocumentSnapshot documentSnapshot) {
//                                    Map<String, Object> library = (Map<String, Object>) documentSnapshot.get("Songs");
//                                    String uniqueKey=musicList.get(currentPos).getKey();
//                                    if (library != null && library.containsKey(uniqueKey)) {
//                                        // Nếu trường tồn tại, xóa nó
//                                        Map<String, Object> updates = new HashMap<>();
//                                        Double k=documentSnapshot.getDouble("songNumber");
//                                        Integer ik=k.intValue();
//                                        Integer updatedNumber=ik-1;
//                                        updates.put("songNumber",updatedNumber);
//                                        updates.put("songs." + uniqueKey, FieldValue.delete());
//
//                                        refLib.update(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                            @Override
//                                            public void onSuccess(Void aVoid) {
//                                                Toast.makeText(MusicPlayer.this,"song removed from your library",Toast.LENGTH_SHORT).show();
//                                            }
//                                        });
//                                    } else {
//                                        // Nếu trường không tồn tại, thêm nó
//                                        Map<String, Object> updates = new HashMap<>();
//                                        Double k=documentSnapshot.getDouble("songNumber");
//                                        Integer ik=k.intValue();
//                                        Integer updatedNumber=ik+1;
//                                        updates.put("songNumber",updatedNumber);
//                                        updates.put("songs." + uniqueKey, musicRef);
//
//                                        refLib.update(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                            @Override
//                                            public void onSuccess(Void aVoid) {
//                                                Toast.makeText(MusicPlayer.this, "Added to library", Toast.LENGTH_SHORT).show();
//                                            }
//                                        });
//                                    }
//                                }
//                            });
                        }
                        return true;
                    }
                });
                popupMenu.show();
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
    }

    //chinh thoi gian
    public String millisecondsToString(int time) {
        int minutes = time / 1000 / 60;
        int seconds = time / 1000 % 60;
        return minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intentSer=new Intent(this,MusicService.class);
        bindService(intentSer,this,BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicService.MyBinder binder=(MusicService.MyBinder)service;
        musicService= binder.getService();
        musicService.setCallback(MusicPlayer.this);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        musicService=null;
        Log.e("Disconnected",musicService+ "");
    }

    public void showNotification(int playPauseBtn, Float playbackSpeed)
    {
        Intent prevIntent=new Intent(this, NotificationReceiver.class).setAction(ACTION_PREV);
        PendingIntent prevPendingIntent=PendingIntent.getBroadcast(this,0,prevIntent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Intent playIntent=new Intent(this,NotificationReceiver.class).setAction(ACTION_PLAY);
        PendingIntent playPendingIntent=PendingIntent.getBroadcast(this,0,playIntent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Intent nextIntent=new Intent(this,NotificationReceiver.class).setAction(ACTION_NEXTS);
        PendingIntent nextPendingIntent=PendingIntent.getBroadcast(this,0,nextIntent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Glide.with(this)
                .asBitmap()
                .load(musicList.get(position).getImage())
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        Notification notification = new NotificationCompat.Builder(MusicPlayer.this, CHANNEL_ID_2)
                                .setSmallIcon(R.drawable.baseline_music_note_24)
                                .setLargeIcon(resource)
                                .setContentTitle(musicList.get(position).getNameSong())
                                .setContentText(musicList.get(position).getSinger())
                                .addAction(R.drawable.baseline_skip_previous_24,"Previous",prevPendingIntent)
                                .addAction(playPauseBtn,"Play",playPendingIntent)
                                .addAction(R.drawable.baseline_skip_next_24,"Next",nextPendingIntent)
                                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                                        .setMediaSession(mediaSession.getSessionToken()))
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setOnlyAlertOnce(true)
                                .build();
                        NotificationManager notificationManager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        notificationManager.notify(0,notification);

                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });
        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION,musicList.get(position).getDuration())
                .build());
        mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING,mMediaPlayer.getCurrentPosition(),playbackSpeed)
                .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                .build());
    }

    @Override
    public void nextClicked() {
        if(isShuffleOn) {
            Random rand = new Random();
            position = rand.nextInt(musicList.size());
            while(playedSongs.contains(position)) {
                position = rand.nextInt(musicList.size());
            }
            playedSongs.add(position);
            if(playedSongs.size() == musicList.size()) {
                playedSongs.clear();
            }
        }
        else {
            if(position<musicList.size()-1)
            {
                position++;
            }
            else {
                position=0;
            }
        }
        currentPos=position;
        initializeMusicPlayer(position);
        if (mMediaPlayer!=null && mMediaPlayer.isPlaying()) {
            showNotification(R.drawable.ic_play,0F);
        } else {
            showNotification(R.drawable.ic_pause,0F);
        }
    }

    @Override
    public void prevClicked() {
        if(position<=0){
            position=musicList.size()-1;
        }
        else {
            position--;
        }
        initializeMusicPlayer(position);
        if (mMediaPlayer!=null && mMediaPlayer.isPlaying()) {
            showNotification(R.drawable.ic_play,0F);
        } else {
            showNotification(R.drawable.ic_pause,0F);
        }
    }

    @Override
    public void playClicked() {
        if (mMediaPlayer!=null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            objectAnimator.pause();
            // change the image of playpause button to play when we pause it
            btnPlay.setBackgroundResource(R.drawable.ic_play);
            showNotification(R.drawable.ic_play,0F);
        } else {
            mMediaPlayer.start();
            // if mediaplayer is playing // the image of play button should display pause
            objectAnimator.resume();
            btnPlay.setBackgroundResource(R.drawable.ic_pause);
            showNotification(R.drawable.ic_pause,0F);
        }
    }


}

