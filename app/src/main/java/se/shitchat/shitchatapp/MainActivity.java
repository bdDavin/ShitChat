package se.shitchat.shitchatapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1337;
    //create fragment fields
    private FloatingActionButton mainFab;
    private RecyclerView chatsRecyclerView;

    private FirestoreRecyclerAdapter<Chat, ChatsViewHolder> adapter;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.mainToolbar));

        //implements firestore database och auth
        mAuth = FirebaseAuth.getInstance();
        String user = mAuth.getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();
        //skapar och kollar login
        createLogInScreen();
        //frågan för databasem
        Query query = db.collection("groups")
                .whereArrayContains("users", user)
                .orderBy("name", Query.Direction.ASCENDING);

        Query query2 = db.collection("groups")
                .document("TskjGm9Muti7c47eN6Gq")
                .collection("messages")
                .orderBy("time", Query.Direction.ASCENDING)
                .limit(1);
        //hämtar data lägger i class
        FirestoreRecyclerOptions<Chat> options = new FirestoreRecyclerOptions.Builder<Chat>()
                .setQuery(query, Chat.class)
                .build();

        mainFab = findViewById(R.id.mainFab);
        chatsRecyclerView = findViewById(R.id.chatsRecyclerView);

        chatsRecyclerView.setHasFixedSize(true);
        chatsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new FirestoreRecyclerAdapter<Chat, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ChatsViewHolder holder, int position, @NonNull Chat chatModel) {
                //sätter datan till viewsen
                holder.chatsUsername.setText(chatModel.getName());

                holder.chatsParent.setOnClickListener(view ->
                        Toast.makeText(getApplicationContext(), chatModel.getName(), Toast.LENGTH_SHORT).show());
            }

            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_chats, viewGroup, false);
                ChatsViewHolder holder = new ChatsViewHolder(view);
                return holder;
            }
        };
        chatsRecyclerView.setAdapter(adapter);


    }

    private class ChatsViewHolder extends RecyclerView.ViewHolder {
        private TextView chatsUsername;
        private TextView lastMessage;
        private LinearLayout chatsParent;

        public ChatsViewHolder(View itemView) {

            super(itemView);

            chatsUsername = itemView.findViewById(R.id.chats_username);
            lastMessage = itemView.findViewById(R.id.chats_last_message);
            chatsParent = itemView.findViewById(R.id.chats_parent);

        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void createLogInScreen() {

        if (mAuth.getCurrentUser() == null) {
            // Här kan vi lägga till fler inloggs alternativ
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.GoogleBuilder().build(),
                    new AuthUI.IdpConfig.EmailBuilder().build());

            // Skapar och vissar inloggs activitetet
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .setIsSmartLockEnabled(false, true)
                            .setLogo(R.mipmap.ic_launcher)
                            .setTheme(R.style.CustomTheme)
                            .build(),
                    RC_SIGN_IN);
        } else {

            showSignedInSnack();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                // Sparar användaren i databasen med Uid
                User user = new User();
                user.setEmail(mAuth.getCurrentUser().getEmail());
                user.setUsername(mAuth.getCurrentUser().getDisplayName());
                user.setImage("default");

                String userUid = mAuth.getCurrentUser().getUid();
                db.collection("users")
                        .document(userUid)
                        .set(user);

                showSignedInSnack();
            } else {
                Snackbar.make(findViewById(R.id.mainToolbar), R.string.log_in_failed,
                        Snackbar.LENGTH_LONG)
                        .show();
            }
        }
    }

    //En snackbar som vissar vem som är inloggad
    private void showSignedInSnack() {
        Snackbar.make(findViewById(R.id.mainToolbar), getString(R.string.logged_in_as) + FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),
                Snackbar.LENGTH_SHORT)
                .show();
    }

    public void newMessage(View view) {
        Intent i = new Intent(this, MessageActivity.class);
        startActivity(i);
    }

    public void enterProfile(MenuItem item) {
        Intent i = new Intent(this, ProfileActivity.class);
        startActivity(i);
    }

    public void logOut(MenuItem item) {
        mAuth.signOut();
        createLogInScreen();
    }
}
