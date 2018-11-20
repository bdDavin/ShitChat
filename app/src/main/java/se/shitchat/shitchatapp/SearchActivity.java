package se.shitchat.shitchatapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
public class SearchActivity extends AppCompatActivity {

    private Toolbar searchToolbar;
    private FirebaseFirestore db;
    private RecyclerView searchRecycler;
    private ImageButton searchButton;
    private SearchAdapter searchAdapter;
    private Query userDb;
    private EditText input;
    private Query query;
    private String searchInput;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        input = findViewById(R.id.editText);
        searchButton = findViewById(R.id.imageButtonSearch);
        searchToolbar = findViewById(R.id.searchToolbar);
        searchRecycler = findViewById(R.id.searchRecyclerView);

        setSupportActionBar(searchToolbar);
        mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();

        //skickar med group id (Singelton ish)
        SearchAdapter.groupID = getIntent().getStringExtra("groupId");

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                searchInput = input.getText().toString();

                userDb = db.collection("users").whereEqualTo("username", searchInput);

                setUpSearchRecycler();

                searchAdapter.startListening();



            }
        });

    }

    private String SearchQuery() {
        searchInput = input.getText().toString();

        return searchInput;
    }


    //Fetches data and adds to recyclerView
    private void setUpSearchRecycler() {

        FirestoreRecyclerOptions<User> option = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(userDb, User.class)
                .build();

        searchAdapter = new SearchAdapter(option);

        RecyclerView searchRecyclerView = findViewById(R.id.searchRecyclerView);

        searchRecyclerView.setHasFixedSize(true);
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchRecyclerView.setAdapter(searchAdapter);


    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();
        searchAdapter.stopListening();
    }
}



