package se.shitchat.shitchatapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;


public class MessageActivity extends AppCompatActivity {

    private ImageButton sendButton;
    private EditText ediMessage;
    private FirebaseFirestore db;
    private RecyclerView messageRecycler;
    private ImageView inputIndicator;
    private MessageAdapter adapter;

    //from group
    private String groupId = "kemywcCWdHKO5ESZpSZn";
    String groupName = "Benjamin test grupp";
    private boolean image;
    private String imageUrl;
    private boolean ImActive;
    private Chat chat;
    private boolean addToChat = false;
    private Boolean groupIsActive = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        setSupportActionBar(findViewById(R.id.messageToolbar));

        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");
        groupName = intent.getStringExtra("groupName");

        if (groupName == null) {
            groupName = "inget namn skickas med";
        }


        initialization();


        //loads input indicator with glide
        Glide.with(this)
                //.asGif()
                .load(getDrawable(R.drawable.typing))
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

        //get group
        // chat = getGroup();

        //change toolbar to groupname
        Objects.requireNonNull(getSupportActionBar()).setTitle(groupName);

        //test for document change
        db.collection("groups").document(groupId).addSnapshotListener((documentSnapshot, e) -> {

            Log.i("display", "Document has been updated");
            //recieves value


            Boolean serverValue = documentSnapshot.getBoolean("active");
            Log.i("display", "servervalue is: " +serverValue);

            //test value
            if (serverValue == null || serverValue == false) {
                groupIsActive = false;
            } else {
                groupIsActive = true;
            }

            displayTyping();
        });


    }

    private Chat getGroup() {
        //frågar databasen efter det senaste meddelandet i gruppen och sätter det i vyn

        final Chat[] group = new Chat[1];

       db.collection("groups")
                .document(groupId).get().addOnCompleteListener(task -> {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                         group[0] = document.toObject(Chat.class);
                         document.get("userNames");

                    }
                });

        return group[0];

    }

    private String getToolbarName() {


        DocumentReference group = db.collection("groups").document(groupId);

        group.getClass();

        // Atomically add a new region to the "UserNames" array field.
        //group.update("userNames", FieldValue.arrayUnion(model.getUsername()));


        if (chat == null) { //TODO chat is null

            Log.i("toolbar", "setToolbar: 1");
            return "Group non existing";
        }
        //if name is not default set name
        else if (!chat.getName().equals("default")) {
            //if name is default set name to users
            Log.i("toolbar", "setToolbar: 2");
            return chat.getName();
        }
        else
            {
            ArrayList<String> names = chat.getUserNames();
            String namesFormat = "";
            //loop all users in grpup and add them to string
            for (int i = 0; i < names.size(); i++) {
                FirebaseAuth mAuth = FirebaseAuth.getInstance();

                //excloude yourself
                if (!names.get(i).equalsIgnoreCase(mAuth.getCurrentUser().getDisplayName())) {
                    namesFormat = namesFormat + names.get(i) + " ";
                }
            }

                Log.i("toolbar", "setToolbar: 3");
            return namesFormat;
        }
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
        ((LinearLayoutManager)messageRecycler.getLayoutManager()).setStackFromEnd(true);
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
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

    }

    private void initialization() {
        db = FirebaseFirestore.getInstance();
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


    //returns true if someone is writing
    private boolean isChatActive() {
        //standard value

        final Boolean[] groupIsActive = new Boolean[1];


        db.collection("groups")
                .document(groupId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    //recieves value
                    Boolean serverValue = documentSnapshot.getBoolean("active");
                    Log.i("display", "servervalue is: " +serverValue);

                    //test value
                    if (serverValue == null || serverValue == false) {
                       groupIsActive[0] = false;
                    } else {
                       groupIsActive[0] = true;
                    }
                });



        Log.i("display", "returning servervalue : " +groupIsActive[0]);

                return groupIsActive[0];

    }

    /************************** Sending picture ************/

    static final int REQUEST_IMAGE_GALLERY = 1337;

    private void openGallery() {
        //open gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
    }

    static private String imageURL;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @android.support.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //if coming back from gallery or camera load image to message
        if ((requestCode == REQUEST_IMAGE_GALLERY //|| requestCode == REQUEST_IMAGE_CAPTURE)
        )&& resultCode == RESULT_OK && data != null && data.getData() != null){

            //get image
            Uri imageUri = data.getData();

            //upload image to firestore
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();;
            StorageReference imagesRef = storageRef.child("images/messages/"+imageUri.getLastPathSegment());
            UploadTask uploadTask = imagesRef.putFile(imageUri);

            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                imagesRef.getDownloadUrl()
                        .addOnCompleteListener(task -> {
                            //saves download link
                            imageURL = task.getResult().toString();
                        });
            });
        }
    }

    /******************* cammera ***************************/

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
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

        if (input.length() <= 0) {
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
        getMenuInflater().inflate(R.menu.message_menu,menu);
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
        //imageUrl = "R.drawable.default_profile";
    }

    //Handles back button
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //this.finish();
    }
}
