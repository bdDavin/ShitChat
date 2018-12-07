package se.shitchat.shitchatapp.activitys;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Objects;

import se.shitchat.shitchatapp.R;
import se.shitchat.shitchatapp.adapters.MessageAdapter;
import se.shitchat.shitchatapp.classes.Message;


public class MessageActivity extends AppCompatActivity {

    private ImageButton sendButton;
    private EditText ediMessage;
    private FirebaseFirestore db;
    private RecyclerView messageRecycler;
    private ImageView inputIndicator;
    private MessageAdapter adapter;
    private String imageURL;

    //from group
    private String groupId = "kemywcCWdHKO5ESZpSZn";
    private boolean ImActive;
    private FirebaseAuth mAuth;

    public static final int PERMISSION_REQUEST_CODE = 1000;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        setSupportActionBar(findViewById(R.id.messageToolbar));

        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");
        String groupName = intent.getStringExtra("groupName");


        initialization();


        //loads input indicator with glide
        Glide.with(this)
                .load(getDrawable(R.drawable.typingicon))
                .into(inputIndicator);


        //sendbutton
        sendButton.setOnClickListener(v1 -> sendButtonPressed());


        //insert items to recycler
        setUpRecyclerView();

        //send message when enter key is pushed
        ediMessage.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN)
            {
                switch (keyCode)
                {
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                    case KeyEvent.KEYCODE_ENTER:
                        sendButtonPressed();
                        return true;
                    default:
                        break;
                }
            }
            return false;
        });


        //Scroll to bottom on new messages
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                Objects.requireNonNull(messageRecycler.getLayoutManager()).smoothScrollToPosition(messageRecycler, null, adapter.getItemCount());
            }
        });

        //change toolbar to groupname
        setToolbarName();

        //Update Has Seen Status
        updateSeenStatus();

        //test for document change
        db.collection("groups").document(groupId).addSnapshotListener((documentSnapshot, e) -> {

            Log.i("display", "Document has been updated");
            //recieves value from fire store
            Boolean serverValue = Objects.requireNonNull(documentSnapshot).getBoolean("active");
            Log.i("display", "servervalue is: " +serverValue);


            //displays indicator
            displayTyping();
        });


    }

    public void updateSeenStatus(){
        db.collection("groups")
                .document(groupId)
                .update(mAuth.getCurrentUser().getUid()+"HasSeen", "true");
    }

    private void setToolbarName() {

        //get group
        db.collection("groups")
                .document(groupId)
                .get()
                .addOnCompleteListener(task -> {
                    DocumentSnapshot document = task.getResult();

                    //test for groupname
                    if (!Objects.requireNonNull(document.getString("name")).equals("default")) {
                        Objects.requireNonNull(getSupportActionBar()).setTitle(document.getString("name"));
                    }
                    else {
                        //sets name to members
                        ArrayList<String> names = (ArrayList<String>) document.get("userNames");
                        StringBuilder groupName = new StringBuilder();
                        for (int i = 0; i < names.size(); i++) {
                            //exclude your username
                            if (!names.get(i).equalsIgnoreCase(mAuth.getCurrentUser().getDisplayName())) {
                                groupName.append(names.get(i)).append(" ");
                            }
                        }
                        //sets name to toolbar
                        Objects.requireNonNull(getSupportActionBar()).setTitle(groupName.toString());
                    }
                });
    }


    private void setUpRecyclerView() {

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        messageRecycler.setLayoutManager(linearLayoutManager);

        //gets message collection
        Query query = db.collection("groups").document(groupId).collection("messages")
                .orderBy("creationDate", Query.Direction.ASCENDING)
                .limit(50);
        //creates recycler
        FirestoreRecyclerOptions<Message> options = new FirestoreRecyclerOptions.Builder<Message>()
                .setQuery(query, Message.class)
                .build();

        //creates adapter from firestore to message bubbles
        adapter = new MessageAdapter(options, this);

        //sets settings for recycler
        messageRecycler.setHasFixedSize(true);
        messageRecycler.setAdapter(adapter);
        ((LinearLayoutManager) messageRecycler.getLayoutManager()).setStackFromEnd(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //starts updating from db
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //stop updating from db
        adapter.stopListening();
        ImActive = false;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

    }

    private void initialization() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        //instances firestore
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .build();
        db.setFirestoreSettings(settings);
        ediMessage = findViewById(R.id.message_edit);
        sendButton = findViewById(R.id.send_button);
        messageRecycler = findViewById(R.id.recyclerView);
        inputIndicator = findViewById(R.id.image_view_active);
        textChanging();




    }







    /********************** Is typing *****************************/


    private void textChanging() {
        //adds input update
        ediMessage.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                Log.i("input", "after");

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.i("input", "before");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                Log.i("display", "My text field has changed");
                if( count >= 1) {
                    Log.i("display", "I am active");
                    ImActive = true;
                    db.collection("groups").document(groupId).update("active", true);


                }
                else if (count == 0) {
                    Log.i("display", "I am inactive");
                    ImActive = false;
                    db.collection("groups").document(groupId).update("active", false);

                }

            }
        });
    }


    private void displayTyping() {
        Boolean groupIsActive = false;
        Log.i("display", "displayTyping: im: " +ImActive +" group is: " + groupIsActive);
        if (!ImActive && groupIsActive) {
            Log.i("display", "buble is VISIBLE");
            inputIndicator.setVisibility(View.VISIBLE);

        }
        else {
            Log.i("display", "buble is GONE");
            inputIndicator.setVisibility(View.GONE);

        }
    }


    /************************** Sending picture  ************/
    private static final int REQUEST_IMAGE_GALLERY = 1337;



    private void openGallery() {

        //test for permission granted
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getBaseContext(), "You should grant permission", Toast.LENGTH_SHORT).show();

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MessageActivity.PERMISSION_REQUEST_CODE);

        }
        else {
            //open gallery
            Intent i = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            startActivityForResult(i, REQUEST_IMAGE_GALLERY);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @android.support.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //if coming back from gallery or camera load image to message
        if ((requestCode == REQUEST_IMAGE_GALLERY) && resultCode == RESULT_OK && data != null && data.getData() != null){

            //get image
            Uri imageUri = data.getData();

            //upload image to firestore
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference imagesRef = storageRef.child("images/messages/"+groupId +"/"+imageUri.getLastPathSegment());
            UploadTask uploadTask = imagesRef.putFile(imageUri);

            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnSuccessListener(taskSnapshot -> imagesRef.getDownloadUrl()
                    .addOnCompleteListener(task -> {
                        //saves download link
                        imageURL = task.getResult().toString();

                        //sends image
                        if (imageURL != null) {
                            sendButtonPressed();
                            showImageSnack();
                        }
                    }));
        }
    }




    /******************* cammera ***************************/
    /* TODO IN PROGRESS */
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }


    /********************** Buttons ***********************/


    private void sendButtonPressed() {

        //get user info
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String name = user.getDisplayName();
        String uid = user.getUid();


        //reads input
        String input = getInput();
        ediMessage.setText("");

        //disables sending empty messages
        if (input.length() <= 0 && Objects.equals(imageURL, "default")) {
            return;
        }
        //creates message
        Message message = new Message();
        message.setMessage(input);
        message.setUserID(uid);
        message.setName(name);
        message.setCreationDate();



        //adds image
        message.setImage(imageURL);
        imageURL = "default";


        //sends message to database
        db.collection("groups").document(groupId)
                .collection("messages").add(message);

        //update the timing on group
        db.collection("groups").document(groupId).update("lastUpdated", message.getCreationDate());

        db.collection("groups")
                .document(groupId)
                .get()
                .addOnCompleteListener(task -> {
                    DocumentSnapshot document = task.getResult();
                    ArrayList<String> userUids = (ArrayList<String>) document.get("userId");

                    for (String userUid:userUids) {
                        if (!userUid.equals(uid)){
                            db.collection("groups")
                                    .document(groupId)
                                    .update(userUid+"HasSeen", "false");
                        }
                    }
                });

    }

    private String getInput() {
        return ediMessage.getText().toString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.message_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void searchUsers(MenuItem item) {
        Intent i = new Intent(this, SearchActivity.class);

        boolean addToChat = true;
        i.putExtra("addToChat", addToChat);

        i.putExtra("groupId", groupId);
        startActivity(i);


    }

    public void settings(MenuItem item) {
        Intent i = new Intent(getApplicationContext(), GroupProfileActivity.class);
        i.putExtra("groupId", groupId);
        startActivity(i);
    }

    public void addImagePressed(View view) {
        openGallery();
    }

    public void cameraButtonPressed() {
        dispatchTakePictureIntent();
    }

    //Handles back button
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //this.finish();
    }

    //En snackbar som vissar vem som är inloggad
    private void showImageSnack() {
        Snackbar.make(ediMessage, getString(R.string.image_sent) ,
                Snackbar.LENGTH_SHORT)
                .show();
    }
}
