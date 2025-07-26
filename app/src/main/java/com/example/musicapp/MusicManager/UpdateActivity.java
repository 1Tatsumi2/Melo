package com.example.musicapp.MusicManager;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.bumptech.glide.Glide;
import com.example.musicapp.Class.Song;
import com.example.musicapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yalantis.ucrop.UCrop;

import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateActivity extends AppCompatActivity {
    CircleImageView updateImage;
    TextView file;
    Button saveButton, audioSave;
    EditText name,artist,singer,album;
    Uri uriImage,uriAu,uriVideo;
    String key,oldImageUrl,oldAudioUrl,oldVideo,imageUrl,audioUrl,videoUpdate;
    MediaPlayer mediaPlayer;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> cropImageLauncher;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    boolean isImageUpdated = false;
    boolean isAudioUpdated = false;
    int duration=0;
    FirebaseFirestore db= FirebaseFirestore.getInstance();
    DocumentReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update);
        updateImage=findViewById(R.id.updateImage);
        file=findViewById(R.id.fileUpdate);
        saveButton=findViewById(R.id.saveUpdateButton);
        audioSave=findViewById(R.id.btnUpdateFile);
        name=findViewById(R.id.updateName);
        artist=findViewById(R.id.updateArtist);

        activityResultLauncher=registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult o) {
                        if(o.getResultCode()== Activity.RESULT_OK)
                        {
                            Intent data=o.getData();
                            uriImage=data.getData();
                            updateImage.setImageURI(uriImage);
                        }
                        else {
                        }
                    }
                }
        );

        //audio
        ActivityResultLauncher<Intent> activityResultLauncherAu=registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult o) {
                        if(o.getResultCode()== Activity.RESULT_OK){
                            Intent data=o.getData();
                            uriAu=data.getData();
                            uriVideo=data.getData();
                            file.setText("File selected");
                        }
                        else {
                        }
                    }
                }
        );

        //camera
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // The image capture intent was successful, start the cropping activity
                        UCrop.of(uriImage, uriImage)
                                .withAspectRatio(1, 1)
                                .start(this);
                        updateImage.setImageURI(uriImage);
                    }
                }
        );

        cropImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // The cropping activity was successful, get the resulting Uri
                        Uri croppedImageUri = UCrop.getOutput(result.getData());
                        // Use croppedImageUri
                    } else if (result.getResultCode() == UCrop.RESULT_ERROR) {
                        Throwable cropError = UCrop.getError(result.getData());
                        // Handle possible errors here
                    }
                }
        );

        Bundle bundle=getIntent().getExtras();
        if(bundle!=null)
        {
          Glide.with(UpdateActivity.this).load(bundle.getString("Image")).into(updateImage);
            name.setText(bundle.getString("Title"));
            singer.setText(bundle.getString("Singer"));
            key=bundle.getString("Key");
            oldImageUrl=bundle.getString("Image");
            oldAudioUrl=bundle.getString("Audio");
            duration=bundle.getInt("Duration");
            oldVideo=bundle.getString("Video");
        }
        imageUrl=oldImageUrl;
        audioUrl=oldAudioUrl;
        videoUpdate = oldVideo;
        updateImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isImageUpdated=true;
                imagePickDialog();
            }
        });
        audioSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAudioUpdated=true;
                Intent photopicker=new Intent(Intent.ACTION_GET_CONTENT);
                photopicker.setType("audio/*");
                activityResultLauncherAu.launch(photopicker);
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(UpdateActivity.this, MusicManagerActivity.class));
        finish();
    }

    private void saveData() {
        String nameUpdate=name.getText().toString();
        String artistUpdate=artist.getText().toString();
        String singerUpdate=singer.getText().toString();
        if(TextUtils.isEmpty(nameUpdate))
        {
            name.setError("Song name cannot be empty");
            return;
        }
        if(TextUtils.isEmpty(artistUpdate))
        {
            artist.setError("Artist cannot be empty");
            return;
        }
        if(TextUtils.isEmpty(singerUpdate))
        {
            singer.setError("Singer cannot be empty");
            return;
        }
        if(!TextUtils.isEmpty(nameUpdate) && !TextUtils.isEmpty(artistUpdate) && !TextUtils.isEmpty(singerUpdate))
        {
            android.app.AlertDialog.Builder builder=new android.app.AlertDialog.Builder(UpdateActivity.this);
            builder.setCancelable(false);
            builder.setView(R.layout.progress_layout);
            android.app.AlertDialog dialog= builder.create();
            dialog.show();
            if((isAudioUpdated && isImageUpdated)&&(uriImage!=null && uriAu!=null))
            {
                StorageReference storageReferenceImg = FirebaseStorage.getInstance().getReference().child("Android Images")
                        .child(uriImage.getLastPathSegment());
                StorageReference storageReferenceAu = FirebaseStorage.getInstance().getReference().child("Audio")
                        .child(uriAu.getLastPathSegment());
                StorageReference storageReferenceVid = FirebaseStorage.getInstance().getReference().child("Video")
                        .child(uriVideo.getLastPathSegment());
                UploadTask uploadTaskImg = storageReferenceImg.putFile(uriImage);
                UploadTask uploadTaskAu = storageReferenceAu.putFile(uriAu);
                UploadTask uploadTaskVid = storageReferenceAu.putFile(uriVideo);


                uploadTaskImg.continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return storageReferenceImg.getDownloadUrl();
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        imageUrl = task.getResult().toString();

                        uploadTaskAu.continueWithTask(taskAu -> {
                            if (!taskAu.isSuccessful()) {
                                throw taskAu.getException();
                            }
                            return storageReferenceAu.getDownloadUrl();
                        }).addOnCompleteListener(taskAu -> {
                            if (taskAu.isSuccessful()) {
                                audioUrl = taskAu.getResult().toString();

                                // Call UploadData() only when both download URLs have been retrieved
                                dialog.dismiss();
                                UpdateData();
                            }
                        });

                        uploadTaskVid.continueWithTask(taskAu -> {
                            if (!taskAu.isSuccessful()) {
                                throw taskAu.getException();
                            }
                            return storageReferenceVid.getDownloadUrl();
                        }).addOnCompleteListener(taskAu -> {
                            if (taskAu.isSuccessful()) {
                                videoUpdate = taskAu.getResult().toString();

                                // Call UploadData() only when both download URLs have been retrieved
                                dialog.dismiss();
                                UpdateData();
                            }
                        });
                    }
                });
            }
            else if (isAudioUpdated && uriAu!=null) {
                StorageReference storageReferenceAu = FirebaseStorage.getInstance().getReference().child("Audio")
                        .child(uriAu.getLastPathSegment());
                storageReferenceAu.putFile(uriAu).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                        uriTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                audioUrl = uri.toString();
                                UpdateData();
                                dialog.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialog.dismiss();
                            }
                        });
                    }
                });

            }
            else if (isImageUpdated && uriImage!=null)
            {
                StorageReference storageReferenceImg = FirebaseStorage.getInstance().getReference().child("Android Images")
                        .child(uriImage.getLastPathSegment());
                storageReferenceImg.putFile(uriImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                        uriTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                imageUrl = uri.toString();
                                UpdateData();
                                dialog.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialog.dismiss();
                            }
                        });
                    }
                });
            }
            else
            {
                UpdateData();
            }
        }
    }
    private void UpdateData() {
        String nameUpdate=name.getText().toString();
        String artistUpdate=artist.getText().toString();
        String albumUpdate="";
        String singerUpdate=singer.getText().toString();
        if((isAudioUpdated && isImageUpdated)&& uriImage==null)
        {
            imageUrl=oldImageUrl;
        }
        if((isAudioUpdated && isImageUpdated) && uriAu==null)
        {
            audioUrl=oldAudioUrl;
        }
        if(isAudioUpdated && uriAu==null)
        {
            audioUrl=oldAudioUrl;
        }
        if(isImageUpdated && uriImage==null)
        {
            imageUrl=oldImageUrl;
        }
        if(isAudioUpdated && uriAu!=null)
        {
            mediaPlayer= MediaPlayer.create(UpdateActivity.this,uriAu);
            duration = mediaPlayer.getDuration();
        }
        Song song=new Song(nameUpdate,singerUpdate,audioUrl,duration,imageUrl,albumUpdate,videoUpdate);
        ref=db.collection("Music").document(key);
        ref.set(song).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(UpdateActivity.this,"Update Success",Toast.LENGTH_SHORT).show();
                Intent i=new Intent(UpdateActivity.this, MusicManagerActivity.class);
                startActivity(i);
                finish();
            }
        });
        //các thao tác xóa file cũ
    }

    private void imagePickDialog() {
        String[] options={"Camera","Gallery"};
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Pick Image From");
        //set items/options
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handle click
                if(which==0)
                {
                    //camera clicked
                    pickFromCamera();
                }
                else if(which==1)
                {
                    pickFromGallery();
                }
            }
        });
        //create/show dialog
        builder.create().show();
    }

    private void pickFromGallery() {
        //intent to pick image from gallary, the image will be rerurn in onActivityResult method
        Intent i=new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        activityResultLauncher.launch(i);
    }

    private void pickFromCamera() {
        ContentValues values=new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Image_TITLE");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Image_DESCRIPTION");
        //put image uri
        uriImage=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        //intent to open camera
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,uriImage);
        cameraLauncher.launch(intent);
    }
}