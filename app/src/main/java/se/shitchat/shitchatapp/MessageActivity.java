package se.shitchat.shitchatapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;


public class MessageActivity extends AppCompatActivity {

    private Button sendButton;
    private EditText ediMessage;
    private FirebaseFirestore db;
    private RecyclerView messageRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        setSupportActionBar(findViewById(R.id.messageToolbar));

        initalization();

        //sendbutton
        sendButton.setOnClickListener(this::sendButtonPressed);


        //insert items to recycler


    }

    private void initalization() {
        db = FirebaseFirestore.getInstance();
        ediMessage = findViewById(R.id.message_edit);
        sendButton = findViewById(R.id.send_button);
    }

    private void sendButtonPressed(View v) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        String name = user.getDisplayName();
        String uid = user.getUid();


        //reads input
        String input = getInput();
        ediMessage.setText("");

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
