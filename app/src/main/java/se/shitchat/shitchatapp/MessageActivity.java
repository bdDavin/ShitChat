package se.shitchat.shitchatapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
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



public class MessageActivity extends AppCompatActivity {

    private ImageButton sendButton;
    private EditText ediMessage;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private RecyclerView messageRecycler;
    private ImageView inputIndicator;
    private MessageAdapter adapter;
    private String imageURL;

    //from group
    private String groupId = "kemywcCWdHKO5ESZpSZn";
    private boolean ImActive;
    private boolean addToChat = false;
    private Boolean groupIsActive = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        setSupportActionBar(findViewById(R.id.messageToolbar));

        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");

        initialization();


        //loads input indicator with glide
        Glide.with(this)
                .load(getDrawable(R.drawable.typingicon))
                .into(inputIndicator);


        //sendbutton
        sendButton.setOnClickListener(this::sendButtonPressed);


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
                        sendButtonPressed(v);
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
                messageRecycler.getLayoutManager().smoothScrollToPosition(messageRecycler, null, adapter.getItemCount());
            }
        });


        //change toolbar to groupname
        setToolbarName();

        //test for document change
        db.collection("groups").document(groupId).addSnapshotListener((documentSnapshot, e) -> {

            Log.i("display", "Document has been updated");
            //recieves value from fire store
            Boolean serverValue = documentSnapshot.getBoolean("active");
            Log.i("display", "servervalue is: " +serverValue);

            //test value
            if (serverValue == null || serverValue == false) {
                groupIsActive = false;
            } else {
                groupIsActive = true;
            }

            //displays indicator
            displayTyping();
        });


    }

    private void setToolbarName() {

        //get group
        db.collection("groups")
                .document(groupId)
                .get()
                .addOnCompleteListener(task -> {
                    DocumentSnapshot document = task.getResult();

                    //test for groupname
                    if (!document.getString("name").equals("default")) {
                        Objects.requireNonNull(getSupportActionBar()).setTitle(document.getString("name"));
                    }
                    else {
                        //sets name to members
                        ArrayList<String> names = (ArrayList<String>) document.get("userNames");
                        String groupName = "";
                        for (int i = 0; i < names.size(); i++) {
                            //exclude your username
                            if (!names.get(i).equalsIgnoreCase(mAuth.getCurrentUser().getDisplayName())) {
                                groupName = groupName + names.get(i) + " ";
                            }
                        }
                        //sets name to toolbar
                        Objects.requireNonNull(getSupportActionBar()).setTitle(groupName);
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
        adapter = new MessageAdapter(options);

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
        Log.i("display", "displayTyping: im: " +ImActive +" group is: " +groupIsActive);
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
    static final int REQUEST_IMAGE_GALLERY = 1337;

    private void openGallery() {
        //open gallery
        Intent i = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

         startActivityForResult(i, REQUEST_IMAGE_GALLERY);
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
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                imagesRef.getDownloadUrl()
                        .addOnCompleteListener(task -> {
                            //saves download link
                            imageURL = task.getResult().toString();

                            //sends image
                            if (imageURL != null) {
                                sendButtonPressed(findViewById(android.R.id.content));
                                showImageSnack();
                            }
                        });
            });
        }
    }




    /******************* cammera ***************************/
    /* TODO IN PROGRESS */
    static final int REQUEST_IMAGE_CAPTURE = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }


    public void fullScreen() {

        // BEGIN_INCLUDE (get_current_ui_flags)
        // The UI options currently enabled are represented by a bitfield.
        // getSystemUiVisibility() gives us that bitfield.
        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;
        // END_INCLUDE (get_current_ui_flags)
        // BEGIN_INCLUDE (toggle_ui_flags)
        boolean isImmersiveModeEnabled =
                ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
        if (isImmersiveModeEnabled) {
            Log.i("image", "Turning immersive mode mode off. ");
        } else {
            Log.i("image", "Turning immersive mode mode on.");
        }

        // Navigation bar hiding:  Backwards compatible to ICS.
        if (Build.VERSION.SDK_INT >= 14) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        // Status bar hiding: Backwards compatible to Jellybean
        if (Build.VERSION.SDK_INT >= 16) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }

        // Immersive mode: Backward compatible to KitKat.
        // Note that this flag doesn't do anything by itself, it only augments the behavior
        // of HIDE_NAVIGATION and FLAG_FULLSCREEN.  For the purposes of this sample
        // all three flags are being toggled together.
        // Note that there are two immersive mode UI flags, one of which is referred to as "sticky".
        // Sticky immersive mode differs in that it makes the navigation and status bars
        // semi-transparent, and the UI flag does not get cleared when the user interacts with
        // the screen.
        if (Build.VERSION.SDK_INT >= 18) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
        //END_INCLUDE (set_ui_flags)
    }




    /********************** Buttons ***********************/


    private void sendButtonPressed(View v) {

        //get user info
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String name = user.getDisplayName();
        String uid = user.getUid();


        //reads input
        String input = getInput();
        ediMessage.setText("");

        //disables sending empty messages
        if (input.length() <= 0 && imageURL == "default") {
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

        addToChat = true;
        i.putExtra("addToChat", addToChat);

        i.putExtra("groupId", groupId);
        startActivity(i);


    }

    public void addImagePressed(View view) {
        openGallery();
    }

    public void cameraButtonPressed(View view) {
        dispatchTakePictureIntent();
    }

    //Handles back button
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    //En snackbar som vissar vem som är inloggad
    private void showImageSnack() {
        Snackbar.make(ediMessage, getString(R.string.image_sent) ,
                Snackbar.LENGTH_SHORT)
                .show();
    }
}
