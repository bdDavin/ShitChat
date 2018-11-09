package se.shitchat.shitchatapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1337;
    //create fragment fields
    private Fragment mainFragment;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private FloatingActionButton mainFab;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.mainToolbar));

        //implements firestore database
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        mainFab = findViewById(R.id.mainFab);

        createLogInScreen();

    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void createLogInScreen() {

        if (mAuth.getCurrentUser() == null) {
            // Choose authentication providers
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build(),
                    new AuthUI.IdpConfig.GoogleBuilder().build());

            // Create and launch sign-in intent
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
                // exempel på sakar att spara om användaren
                Map<String, Object> user = new HashMap<>();
                user.put("name", mAuth.getCurrentUser().getDisplayName());
                user.put("email", mAuth.getCurrentUser().getEmail());
                user.put("logged_in_method", mAuth.getCurrentUser().getProviderId());

                String userUid = mAuth.getCurrentUser().getUid();
                db.collection("users")
                        .document(userUid)
                        .set(user);

                showSignedInSnack();
            } else {
                Snackbar.make(findViewById(R.id.mainToolbar), "Log in failed, try again later",
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

    public FirebaseFirestore getDb() {
        return db;
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
