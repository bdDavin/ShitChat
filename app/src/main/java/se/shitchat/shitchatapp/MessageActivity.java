package se.shitchat.shitchatapp;

import android.content.Intent;
import android.os.Bundle;
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


import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;


import java.util.ArrayList;
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
    private boolean active;
    private Chat chat;


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


        //sendbutton
        sendButton.setOnClickListener(this::sendButtonPressed);


        //insert items to recycler
        setUpRecyclerView();

        //send message when enter key is pushed
        ediMessage.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
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

        db.collection("groups").document(groupId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
               displayTyping();
                Log.i("display", "onEvent: displayTyping");
            }
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
        active = false;
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

                Log.i("input", "on");
                if( s.length() >= 1) {
                    Log.i("input", "true");
                    db.collection("groups").document(groupId).update("active", true);
                    active = true;

                }
                else if (s.length() == 0) {
                    db.collection("groups").document(groupId).update("active", false);
                    active = false;
                    Log.i("input", "false");
                }

            }
        });
    }

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
        if (image) {
            message.setImage(imageUrl);
        }

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
        i.putExtra("groupId", groupId);
        startActivity(i);
    }

    public void addImagePressed(View view) {
        image = !image;
        imageUrl = "R.drawable.default_profile";
    }



    private void displayTyping() {
        if (!active && isChatActive()) {
            inputIndicator.setVisibility(View.VISIBLE);
            Log.i("display", "displayTyping: VISIBLE");
        }
        else {
            inputIndicator.setVisibility(View.GONE);
            Log.i("display", "displayTyping: GONE");
        }
    }

    private boolean isChatActive() { 
         Boolean a = false;

                db.collection("groups").document(groupId).get().addOnCompleteListener(task -> {

                    Log.i("display", "someone is active");
                    //TODO returns null
                    // a = task.getResult().getBoolean("active");
                });

        if (a == null) {
            Log.i("display", "everything is null");
            return false;
        }
        else {
            return a;
        }
    }
}
