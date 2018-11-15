package se.shitchat.shitchatapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;


public class MessageActivity extends AppCompatActivity {

    private Button sendButton;
    private EditText ediMessage;
    private FirebaseFirestore db;
    private RecyclerView messageRecycler;
    private CollectionReference messages;
    private MessageAdapter adapter;
    private final String currentGroup = "testChat";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        setSupportActionBar(findViewById(R.id.messageToolbar));

        initalization();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        messageRecycler.setLayoutManager(linearLayoutManager);

        //sendbutton
        sendButton.setOnClickListener(this::sendButtonPressed);


        //insert items to recycler
        setUpRecyclerView();

        //send message when enter key is psuhed
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

    }

    private void setUpRecyclerView() {
        Query query = messages
                .orderBy("creationDate", Query.Direction.ASCENDING)
                .limit(50);

        FirestoreRecyclerOptions<Message> options = new FirestoreRecyclerOptions.Builder<Message>()
                .setQuery(query, Message.class)
                .build();

        adapter = new MessageAdapter(options);

        messageRecycler.setHasFixedSize(true);
        messageRecycler.setLayoutManager(new LinearLayoutManager(this));
        messageRecycler.setAdapter(adapter);
        ((LinearLayoutManager)messageRecycler.getLayoutManager()).setStackFromEnd(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private void initalization() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .build();
        firestore.setFirestoreSettings(settings);
        db = FirebaseFirestore.getInstance();
        ediMessage = findViewById(R.id.message_edit);
        sendButton = findViewById(R.id.send_button);
        messageRecycler = findViewById(R.id.recyclerView);
        messages = db.collection("messages");
    }

    private void sendButtonPressed(View v) {

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
        Message message = new Message(input);
        message.setUserID(uid);
        message.setName(name);

        //sends message to database
        db.collection("messages").add(message);
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
        startActivity(i);
    }


}
