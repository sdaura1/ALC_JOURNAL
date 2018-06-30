package com.sani.shaheed.y.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.sani.shaheed.y.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EntryActivity extends AppCompatActivity {

    private ImageButton mSelectImage;
    private EditText mEntryTitle;
    private EditText mEntryContent;
    private Button mAddEntryBtn;

    Map<String, Object> firestoreData;

    private Uri mImageUri = null;

    private static final int GALLERY_REQUEST = 1;

    private FirebaseStorage fStorage;
    private FirebaseFirestore firestoreRef;
    private StorageReference storageReference;
    private FirebaseFirestoreSettings settings;

    private ProgressDialog mProgress;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        // inittialization of firestore settings
        settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();

        // initialization of firebase Auth and also getting a current user
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        // initialization of firebase firestore, storage, storagereference and hashmap for data to put in firestore
        fStorage = FirebaseStorage.getInstance();
        storageReference = fStorage.getReference();
        firestoreRef = FirebaseFirestore.getInstance();
        firestoreData = new HashMap<>();

        // initialization of widgets
        mSelectImage = findViewById(R.id.imgBtn);
        mEntryTitle = findViewById(R.id.titleField);
        mEntryContent = findViewById(R.id.contentField);
        mAddEntryBtn = findViewById(R.id.addBtn);
        mProgress = new ProgressDialog(this);


        // checking of there's a network connection via wireless or mobile data
        if (isNetworkConnected() || isWifiConnected()) {
            Toast.makeText(this, "Yes you can post", Toast.LENGTH_SHORT).show();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("No Internet Connection")
                    .setCancelable(false)
                    .setMessage("It looks like your internet connection is off. Please turn it " +
                            "on and try again to be able to write your note")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).setIcon(R.drawable.warning).show();
        }

        // selecting image from the device gallery
        mSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);

            }
        });

        mAddEntryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                createEntry();
            }
        });
    }

    // method for creating a new entry into firestore
    private void createEntry() {

        mProgress.setMessage("Inserting to Diary....");
        final String the_title = mEntryTitle.getText().toString().trim();
        final String contents = mEntryContent.getText().toString().trim();

        if(!TextUtils.isEmpty(the_title) && !TextUtils.isEmpty(contents) && mImageUri != null){  //Check if all content is provided
            mProgress.show();

            // creating path for storing image
            StorageReference filepath = storageReference.child("pictures/").child(mImageUri.getLastPathSegment()); // Provide Firebase filepath

            // saving the rest of the data if the file (image) upload is successful
            filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() { //Add file to Firebase
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Task<Uri> dowloadUri = taskSnapshot.getStorage().getDownloadUrl(); //Get file url from firebase

                    firestoreData.put("Title", the_title);
                    firestoreData.put("Content", contents);
                    firestoreData.put("Picture", dowloadUri.toString());
                    firestoreData.put("UID", mCurrentUser.getUid());
                    firestoreData.put("Date", new Date().toString().substring(0, 10) + new Date().toString().substring(23, 28));

                    //saving the rest of the data in a collection in firebase that is pointed by a user document
                    firestoreRef.collection("Journal Entry").document(mCurrentUser.toString()).collection("My Entry").document().set(firestoreData)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(getApplicationContext(), "Successfully Added",
                                                Toast.LENGTH_LONG).show();
                                    }else {
                                        Toast.makeText(getApplicationContext(), "Not Added",
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                    mProgress.dismiss();

                    startActivity(new Intent(EntryActivity.this, DisplayActivity.class));

                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){

            mImageUri = data.getData();

            CropImage.activity(mImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(16,9)
                    .start(this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                mSelectImage.setImageURI(resultUri);
                mImageUri = resultUri;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private boolean isWifiConnected() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE); // 1
        assert connMgr != null;
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo(); // 2
        return networkInfo != null && networkInfo.isConnected(); // 3
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connMgr != null;
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && (ConnectivityManager.TYPE_WIFI == networkInfo.getType()) && networkInfo.isConnected();
    }
}