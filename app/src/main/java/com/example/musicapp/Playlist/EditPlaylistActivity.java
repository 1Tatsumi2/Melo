package com.example.musicapp.Playlist;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
//import android.os.Bundle;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

//import com.MusicManager.UpdateActivity;
//import com.SettingAcc.EditProfileActivity;
//import com.SettingAcc.SettingAccActivity;
import com.bumptech.glide.Glide;
import com.example.musicapp.Class.Playlist;
import com.example.musicapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yalantis.ucrop.UCrop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class
EditPlaylistActivity extends AppCompatActivity {

    EditText names, desc;
    Spinner spinner;
    Switch publicSwitch;
    Button editPlaylist;
    FirebaseAuth fAuth;
    Uri uriImage;
    FirebaseFirestore db=FirebaseFirestore.getInstance();
    CollectionReference refUser=db.collection("users");
    DocumentReference ref;
    CircleImageView updateImage;
    Boolean Ispublic;
    boolean isImageUpdated = false;
    String name,description,classified,oldImageUrl,imageUrl,key,UserID;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> cropImageLauncher;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_playlist);
        Intent intent=getIntent();
        Bundle extraData=intent.getExtras();
        name=extraData.getString("name");
        description=extraData.getString("description");
        Ispublic=extraData.getBoolean("public",false);
        classified=extraData.getString("classified");
        oldImageUrl=extraData.getString("image");
        key=extraData.getString("key");
        imageUrl=oldImageUrl;
        spinner=findViewById(R.id.editSpinner);
        editPlaylist=findViewById(R.id.editPlaylist);
        updateImage=findViewById(R.id.EditImagePlaylist);
        names=findViewById(R.id.EditPlaylistName);
        desc=findViewById(R.id.EditPlaylistDesc);
        publicSwitch=findViewById(R.id.editIsPublic);
        publicSwitch.setChecked(Ispublic);
        Glide.with(EditPlaylistActivity.this).load(imageUrl).into(updateImage);
        names.setText(name);
        desc.setText(description);
        fAuth=FirebaseAuth.getInstance();
        UserID=fAuth.getCurrentUser().getUid();
        publicSwitch.setVisibility(View.GONE);
        updateImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isImageUpdated=true;
                imagePickDialog();
            }
        });
        refUser.document(UserID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String role=documentSnapshot.getString("role");
                if(!role.equals("User"))
                {
                    if(Objects.equals(classified, "Artist Story"))
                    {
                        spinner.setVisibility(View.GONE);
                    }
                    else
                    {
                        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                String item=parent.getItemAtPosition(position).toString();
                                classified=item;
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                String defaultItem = "My Playlist";
                                classified=defaultItem;
                            }
                        });
                        ArrayList<String> arrayList=new ArrayList<>();
                        if(Objects.equals(classified, "Playlist"))
                        {
                            arrayList.add("Playlist");
                            arrayList.add("Explore");
                            arrayList.add("Top 100");
                            arrayList.add("Top Singer");
                            arrayList.add("My Playlist");
                        }
                        else if(Objects.equals(classified, "Top 100"))
                        {
                            arrayList.add("Top 100");
                            arrayList.add("Explore");
                            arrayList.add("Playlist");
                            arrayList.add("Top Singer");
                            arrayList.add("My Playlist");
                        }
                        else if(Objects.equals(classified, "Top Singer"))
                        {
                            arrayList.add("Top Singer");
                            arrayList.add("Explore");
                            arrayList.add("Top 100");
                            arrayList.add("Playlist");
                            arrayList.add("My Playlist");
                        }
                        else if(Objects.equals(classified, "My Playlist"))
                        {
                            arrayList.add("My Playlist");
                            arrayList.add("Explore");
                            arrayList.add("Top Singer");
                            arrayList.add("Top 100");
                            arrayList.add("Playlist");
                        }
                        else if(Objects.equals(classified, "Explore"))
                        {
                            arrayList.add("Explore");
                            arrayList.add("My Playlist");
                            arrayList.add("Top Singer");
                            arrayList.add("Top 100");
                            arrayList.add("Playlist");
                        }
                        ArrayAdapter<String> adapter=new ArrayAdapter<>(EditPlaylistActivity.this, android.R.layout.simple_spinner_item,arrayList);
                        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
                        spinner.setAdapter(adapter);
                    }
                }
                else {
                    classified="My Playlist";
                    spinner.setVisibility(View.GONE); // Ẩn spinner nếu vai trò của người dùng là "user"
                }
            }
        });
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
                            Toast.makeText(EditPlaylistActivity.this,"No Image selected",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
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
        editPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });

    }

    private void saveData() {
        String nameEdit=names.getText().toString();
        String descs=desc.getText().toString();
        if(TextUtils.isEmpty(nameEdit))
        {
            names.setError("Name cannot be empty");
            return;
        }
        if(TextUtils.isEmpty(descs))
        {
            desc.setError("Description cannot be empty");
            return;
        }
        if(uriImage==null)
        {
            imageUrl=oldImageUrl;
        }
        if(!TextUtils.isEmpty(descs) && !TextUtils.isEmpty(nameEdit))
        {
            android.app.AlertDialog.Builder builder=new android.app.AlertDialog.Builder(EditPlaylistActivity.this);
            builder.setCancelable(false);
            builder.setView(R.layout.progress_layout);
            android.app.AlertDialog dialog= builder.create();
            dialog.show();
            if(isImageUpdated && uriImage!=null)
            {
                StorageReference storageReferenceImg = FirebaseStorage.getInstance().getReference().child("Playlist Images")
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
        if(uriImage==null)
        {
            imageUrl=oldImageUrl;
        }
        Playlist playlist=new Playlist(names.getText().toString(),desc.getText().toString(),publicSwitch.isChecked(),imageUrl,classified);
        Map<String, Object> playlistMap = new HashMap<>();
        playlistMap.put("name", playlist.getName());
        playlistMap.put("description", playlist.getDescription());
        playlistMap.put("public", playlist.getPublic());
        playlistMap.put("image", playlist.getImage());
        playlistMap.put("classified", playlist.getClassified());
        ref=db.collection("Playlist").document(key);
        ref.update(playlistMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(EditPlaylistActivity.this, "Update success", Toast.LENGTH_SHORT).show();
                if(!Objects.equals(imageUrl, oldImageUrl))
                {
                    FirebaseStorage storage=FirebaseStorage.getInstance();
                    StorageReference imageReference = storage.getReferenceFromUrl(oldImageUrl);
                    imageReference.delete();
                }
                finish();
            }
        });
    }

    private void imagePickDialog() {
        //option to display in dialog
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
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}