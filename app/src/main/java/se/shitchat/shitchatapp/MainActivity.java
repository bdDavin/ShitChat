package se.shitchat.shitchatapp;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import se.shitchat.shitchatapp.fragments.MainFragment;
import se.shitchat.shitchatapp.fragments.MessageFragment;

public class MainActivity extends AppCompatActivity {

    //create fragment fields
    private Fragment messageFragment;
    private Fragment mainFragment;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //implements firestore database
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        //creates fragment
        messageFragment = new MessageFragment();
        mainFragment = new MainFragment();


        //fragment transaction
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.main_frame, mainFragment);
        fragmentTransaction.commit();



        // Create a new user with a first and last name
        Map<String, Object> user = new HashMap<>();
        user.put("first", "Ben");
        user.put("last", "Davin");
        user.put("born", 1996);

// Add a new document with a generated ID
        db.collection("users")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("Test", "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Test", "Error adding document", e);
                    }
                });

        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment selectedFragment = null;
                        switch (item.getItemId()) {
                            case R.id.action_search:
                                //TODO Search fragment
                                //selectedFragment = ItemOneFragment.newInstance();
                                break;
                            case R.id.action_messages:
                                selectedFragment = messageFragment;
                                break;
                        }
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.add(R.id.main_frame, selectedFragment);
                        transaction.commit();
                        return true;
                    }


                });
    }


    public FirebaseFirestore getDb() {return db;}
}
