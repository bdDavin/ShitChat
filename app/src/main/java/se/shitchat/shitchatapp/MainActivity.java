package se.shitchat.shitchatapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
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

import java.util.ArrayList;
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
    private String userUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.mainToolbar));

        //implements firestore database och auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        mainFab = findViewById(R.id.mainFab);
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

    public void initRecycler() {
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
        adapter = new FirestoreRecyclerAdapter<Chat, ChatsViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull ChatsViewHolder holder, int position, @NonNull Chat chatModel) {

                //sätter datan till namnet
                if (!chatModel.getName().equals("default")) {
                    holder.chatsUsername.setText(chatModel.getName());
                } else {

                    //ändrar namn till medlemmarna
                    ArrayList<String> names = chatModel.getUserNames();
                    String namesFormat = "";
                    for (int i = 0; i < names.size(); i++) {
                        if (!names.get(i).equalsIgnoreCase(mAuth.getCurrentUser().getDisplayName())) {
                            namesFormat = namesFormat + names.get(i) + " ";
                        }
                    }
                    holder.chatsUsername.setText(namesFormat);
                }
                String imageUrl = chatModel.getImage();
                String groupId = getSnapshots().getSnapshot(position).getId();

                //displays image
                if (imageUrl == null || imageUrl.equals("default")) {
                    ArrayList<String> ids = chatModel.getUserId();

                    //displays user profile if only two members
                    if (ids.size() == 2) {
                        for (int i = 0; i < ids.size(); i++) {
                            if (!ids.get(i).equals(mAuth.getCurrentUser().getUid()))
                                db.collection("users")
                                        .document(ids.get(i))
                                        .get()
                                        .addOnSuccessListener(documentSnapshot -> {

                                            //get image from firebase
                                            String friendURL = documentSnapshot.getString("image");
                                            if (friendURL == null || friendURL.equals("default")) {
                                                holder.profileImage.setImageResource(R.drawable.default_profile);
                                            } else {
                                                Picasso.get().load(friendURL).into(holder.profileImage);
                                            }
                                        });
                        }
                    } else {
                        holder.profileImage.setImageResource(R.drawable.default_profile);
                    }
                } else {
                    Picasso.get().load(imageUrl).into(holder.profileImage);
                }
                //frågar databasen efter det senaste meddelandet i gruppen och sätter det i vyn
                db.collection("groups")
                        .document(groupId)
                        .collection("messages")
                        .orderBy("creationDate", Query.Direction.DESCENDING)
                        .limit(1)
                        .addSnapshotListener((snapshot, e) -> {
                            if (!snapshot.isEmpty()) {
                                String m = snapshot.getDocuments().get(0).getString("message");
                                holder.lastMessage.setText(m);
                                holder.date.setText(chatModel.getLastUpdated());
                            }
                        });
                //sätter en onClick på alla items så när man klickar öppnas meddelandeaktivitetn
                //och skickar med grupp dokumentets namn
                holder.chatsParent.setOnClickListener(v -> {
                    Intent i = new Intent(getApplicationContext(), MessageActivity.class);
                    i.putExtra("groupId", groupId);
                    i.putExtra("groupName", holder.chatsUsername.getText());
                    startActivity(i);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    //temporär för att visa vilket grupp id som skickas med
                    Toast.makeText(getApplicationContext(), groupId, Toast.LENGTH_SHORT).show();
                });
            }

            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_chats, viewGroup, false);
                return new ChatsViewHolder(view);
            }
        };
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
    public void createLogInScreen() {

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
                user.setEmail(mAuth.getCurrentUser().getEmail());
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
        Snackbar.make(findViewById(R.id.mainToolbar), getString(R.string.logged_in_as) + " " + mAuth.getCurrentUser().getDisplayName(),
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
        mAuth.signOut();
        createLogInScreen();
    }

    private class ChatsViewHolder extends RecyclerView.ViewHolder {
        private TextView chatsUsername;
        private TextView lastMessage;
        private ImageView profileImage;
        private ConstraintLayout chatsParent;
        private TextView date;

        public ChatsViewHolder(View itemView) {
            super(itemView);

            chatsUsername = itemView.findViewById(R.id.chats_username);
            lastMessage = itemView.findViewById(R.id.chats_last_message);
            profileImage = itemView.findViewById(R.id.profile_image);
            chatsParent = itemView.findViewById(R.id.chats_parent);
            date = itemView.findViewById(R.id.chats_date);
        }
    }
}
