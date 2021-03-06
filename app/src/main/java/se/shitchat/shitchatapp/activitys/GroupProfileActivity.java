package se.shitchat.shitchatapp.activitys;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

import se.shitchat.shitchatapp.R;

public class GroupProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private TextView groupName;
    private ImageView groupImage;
    private EditText editText;
    private String groupId;
    private String groupMembers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_profile);

        Toolbar t = findViewById(R.id.groupProfileToolbar);
        setSupportActionBar(t);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.chat_settings);

        groupName = findViewById(R.id.groupNameText);
        groupImage = findViewById(R.id.groupProfile_image);
        groupImage.setOnClickListener(v -> changeProfileImage());
        groupId = getIntent().getStringExtra("groupId");
        editText = findViewById(R.id.editTextName);

        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        //displays username
        displayGroupName();
        //displays picture if it exists
        setProfileImage();

        editText.setVisibility(View.INVISIBLE);
        editText.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                    case KeyEvent.KEYCODE_ENTER:
                        editName();
                        return true;
                    default:
                        break;
                }
            }
            return false;
        });

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
                        db.collection("groups")
                                .document(groupId)
                                .update("image", URL);
                        setProfileImage();
                        Log.d("hej", "onActivityResult: "+ URL);
                    }));
        }
    }

    private void displayGroupName() {
        db.collection("groups")
                .document(groupId)
                .get()
                .addOnCompleteListener(task -> {
                    DocumentSnapshot document = task.getResult();
                    if (!document.getString("name").equals("default")) {
                        groupName.setText(document.getString("name"));
                    } else {

                        ArrayList<String> names = (ArrayList<String>) document.get("userNames");
                        groupMembers = getGroupMembers(names);
                        groupName.setText(groupMembers);
                    }
                });
    }

    @NonNull
    private String getGroupMembers(ArrayList<String> names) {
        StringBuilder groupNames = new StringBuilder();
        for (int i = 0; i < Objects.requireNonNull(names).size(); i++) {
            if (!names.get(i).equalsIgnoreCase(mAuth.getCurrentUser().getDisplayName())) {
                groupNames.append(names.get(i)).append(" ");
            }
        }
        return groupNames.toString();
    }

    private void setProfileImage(){
        db.collection("groups")
                .document(groupId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String imageUrl = documentSnapshot.getString("image");

                    //displays group image
                    if (imageUrl == null || imageUrl.equals("default")) {
                        groupImage.setImageResource(R.drawable.default_profile);
                    } else {
                        Picasso.get().load(imageUrl).into(groupImage);
                    }
                });
    }

    public void changeName(View view) {
        editText.setText(groupName.getText());
        groupName.setVisibility(View.INVISIBLE);
        editText.setVisibility(View.VISIBLE);
    }

    private void editName(){
        db.collection("groups")
                .document(groupId)
                .update("name", editText.getText().toString());
        editText.setVisibility(View.INVISIBLE);
        groupName.setVisibility(View.VISIBLE);
        displayGroupName();
    }

}
