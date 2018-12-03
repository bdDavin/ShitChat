package se.shitchat.shitchatapp.activitys;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import se.shitchat.shitchatapp.R;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private ImageView userImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar t = findViewById(R.id.profileToolbar);
        setSupportActionBar(t);
        TextView userName = findViewById(R.id.usernameText);
        userImage = findViewById(R.id.profile_image);
        userImage.setOnClickListener(v -> changeProfileImage());

        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        //displays username
        userName.setText(Objects.requireNonNull(mAuth.getCurrentUser()).getDisplayName());
        setProfileImage();
    }

    private void changeProfileImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1337);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1337 && resultCode == RESULT_OK && data != null && data.getData() != null){
            Uri imageUri = data.getData();

            StorageReference imagesRef = storageRef.child("images/"+ imageUri.getLastPathSegment());
            UploadTask uploadTask = imagesRef.putFile(imageUri);
            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnSuccessListener(taskSnapshot -> imagesRef.getDownloadUrl()
                    .addOnCompleteListener(task -> {
                        String URL = Objects.requireNonNull(task.getResult()).toString();
                        db.collection("users")
                                .document(mAuth.getCurrentUser().getUid())
                                .update("image", URL);
                        setProfileImage();
                        Log.d("hej", "onActivityResult: "+ URL);
                    }));
        }
    }
    private void setProfileImage(){
        db.collection("users")
                .document(mAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String imageUrl = documentSnapshot.getString("image");

                    //displays user image
                    if (imageUrl == null || imageUrl.equals("default")) {
                        userImage.setImageResource(R.drawable.default_profile);
                    } else {
                        Picasso.get().load(imageUrl).into(userImage);
                    }
                });
    }
}
