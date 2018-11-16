package se.shitchat.shitchatapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;


public class SearchActivity extends AppCompatActivity {

    private Toolbar t;
    private String userInput;
    private FirebaseFirestore userDatabase;
    private Query query;
    private RecyclerView searchRecycler;
    private ImageButton search;
    private List<User> userList;
    private SearchAdapter searchAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        userDatabase = FirebaseFirestore.getInstance();

        searchAdapter = new SearchAdapter(userList);

        userList = new ArrayList<>();

        t = findViewById(R.id.searchToolbar);
        setSupportActionBar(t);

        searchRecycler = findViewById(R.id.searchRecyclerView);
        searchRecycler.setHasFixedSize(true);
        searchRecycler.setLayoutManager(new LinearLayoutManager(this));
        searchRecycler.setAdapter(searchAdapter);

        search = findViewById(R.id.imageButtonSearch);

        searchRecycler.setHasFixedSize(true);
        searchRecycler.setLayoutManager(new LinearLayoutManager(this));

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ListenerRegistration users = userDatabase.collection("users").addSnapshotListener(new EventListener<QuerySnapshot>() {
                    private static final String TAG = "Firelog";


                    @Override
                    public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {


                        if (e != null) {

                            Log.d(TAG, "bror" + e.getMessage());

                        }

                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                           if (doc.getType() == DocumentChange.Type.ADDED) {

                               User user = doc.getDocument().toObject(User.class);
                               userList.add(user);

                               String username = doc.getDocument().getString("name");

                               //Log.d(TAG, "Name: " + userList);

                               Log.d(TAG, "Name: " + username);

                               searchAdapter.notifyDataSetChanged();

                           }

                        }

                    }

                });

            }

        });

    }

}

