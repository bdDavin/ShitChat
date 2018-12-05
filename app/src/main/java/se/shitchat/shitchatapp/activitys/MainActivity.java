package se.shitchat.shitchatapp.activitys;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

//shitchat
import dmax.dialog.SpotsDialog;
import se.shitchat.shitchatapp.SaveImageHelper;
import se.shitchat.shitchatapp.adapters.ChatRecyclerAdapter;
import se.shitchat.shitchatapp.classes.Chat;
import se.shitchat.shitchatapp.R;
import se.shitchat.shitchatapp.SwipeToDeleteCallback;
import se.shitchat.shitchatapp.adapters.ChatRecyclerAdapter;
import se.shitchat.shitchatapp.classes.Chat;
import se.shitchat.shitchatapp.classes.User;
import se.shitchat.shitchatapp.holders.ChatsViewHolder;

//firebase
//shitchat

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1337;
    private RecyclerView chatsRecyclerView;

    private FirestoreRecyclerAdapter<Chat, ChatsViewHolder> adapter;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.mainToolbar));

        //implements firestore database och auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //create fragment fields
        FloatingActionButton mainFab = findViewById(R.id.mainFab);
        chatsRecyclerView = findViewById(R.id.chatsRecyclerView);

        //skapar och kollar login
        createLogInScreen();
        swipeToDelete();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // recyclerview start updating
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        //stops recyclerview from updating
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    private void initRecycler() {
        //frågan för databasen
        Query query = db.collection("groups")
                .whereArrayContains("userId", userUid)
                .orderBy("lastUpdated", Query.Direction.DESCENDING);

        //hämtar datan lägger i Chat.class
        FirestoreRecyclerOptions<Chat> options = new FirestoreRecyclerOptions.Builder<Chat>()
                .setQuery(query, Chat.class)
                .build();

        chatsRecyclerView.setHasFixedSize(true);
        chatsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //Skapar adaptern
        adapter = new ChatRecyclerAdapter(options) ;

        chatsRecyclerView.setAdapter(adapter);
    }

    private void swipeToDelete() {
        //Add Swipe to delete functionality
        SwipeToDeleteCallback swipeHandler = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                //adapter.removeItem(viewHolder.getAdapterPosition());
                String adapterPosId = adapter.getSnapshots().getSnapshot(viewHolder.getAdapterPosition()).getId();
                db.collection("groups")
                        .document(adapterPosId)
                        .collection("messages")
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            List<DocumentSnapshot> m = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot mId : m) {
                                db.collection("groups")
                                        .document(adapterPosId)
                                        .collection("messages")
                                        .document(mId.getId())
                                        .delete();
                            }
                        });
                db.collection("groups")
                        .document(adapter.getSnapshots().getSnapshot(viewHolder.getAdapterPosition()).getId())
                        .delete()
                        .addOnSuccessListener(aVoid -> adapter.notifyItemRemoved(viewHolder.getAdapterPosition()));
            }
        };
        ItemTouchHelper touchHelper = new ItemTouchHelper(swipeHandler);
        touchHelper.attachToRecyclerView(chatsRecyclerView);
    }

    //Skapar login Aktiviteten
    private void createLogInScreen() {

        if (mAuth.getCurrentUser() == null) {
            //inloggs alternativ
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
            userUid = mAuth.getCurrentUser().getUid();
            initRecycler();
            showSignedInSnack();
        }
    }

    @Override //Kollar om man inlogg gick igenom
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                //Skapar en User.class instans
                User user = new User();
                user.setEmail(Objects.requireNonNull(mAuth.getCurrentUser()).getEmail());
                user.setUsername(mAuth.getCurrentUser().getDisplayName());
                user.setImage("default");
                // Sparar användaren i databasen med Uid
                String userUid = mAuth.getCurrentUser().getUid();
                db.collection("users")
                        .document(userUid)
                        .set(user);

                FirebaseInstanceId.getInstance().getInstanceId()
                        .addOnSuccessListener(instanceIdResult -> db.collection("users")
                                .document(mAuth.getCurrentUser().getUid())
                                .update("deviceToken", instanceIdResult.getToken())
                                .addOnSuccessListener(aVoid -> showSignedInSnack()));
            } else {
                showLoginFailedSnack();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //En snackbar som vissar vem som är inloggad
    private void showSignedInSnack() {
        Snackbar.make(findViewById(R.id.mainToolbar), getString(R.string.logged_in_as) + " " + Objects.requireNonNull(mAuth.getCurrentUser()).getDisplayName(),
                Snackbar.LENGTH_SHORT)
                .show();
    }

    //En snackbar
    private void showLoginFailedSnack() {
        Snackbar.make(findViewById(R.id.mainToolbar), R.string.log_in_failed,
                Snackbar.LENGTH_LONG)
                .show();
    }

    public void newMessage(View view) {
        Intent i = new Intent(this, SearchActivity.class);
        startActivity(i);
    }

    public void enterProfile(MenuItem item) {
        Intent i = new Intent(this, ProfileActivity.class);
        startActivity(i);
    }

    public void logOut(MenuItem item) {

            //build dialog box
            AlertDialog.Builder logoutBuilder = new AlertDialog.Builder(this, R.style.LightDialogTheme);
            logoutBuilder.setMessage("Do you want to logout?")
                    .setCancelable(false)

                    //on click listener
                    .setPositiveButton("Yes", (dialog, which) -> {

                        //logout
                        mAuth.signOut();
                        createLogInScreen();

                    })
                    //onclick listener
                    .setNegativeButton("No", (dialog, which) -> {

                    });

            //show download dialog
            AlertDialog logoutDialog = logoutBuilder.create();
            logoutDialog.setTitle("Logout");
            logoutDialog.show();
    }

}
