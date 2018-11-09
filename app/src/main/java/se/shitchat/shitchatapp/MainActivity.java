package se.shitchat.shitchatapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.shitchat.shitchatapp.fragments.MainFragment;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1337;
    //create fragment fields
    private Fragment mainFragment;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //implements firestore database
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        createLogInScreen();
    }
//        BottomNavigationView bottomNavigationView = (BottomNavigationView)
//                findViewById(R.id.bottomNavigationView);
//
//        bottomNavigationView.setOnNavigationItemSelectedListener
//                (new BottomNavigationView.OnNavigationItemSelectedListener() {
//                    @Override
//                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                        Fragment selectedFragment = null;
//                        switch (item.getItemId()) {
//                            case R.id.action_search:
//                                //TODO Search fragment
//                                //selectedFragment = ItemOneFragment.newInstance();
//                                break;
//                            case R.id.action_messages:
//                                selectedFragment = messageFragment;
//                                break;
//                        }
//                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//                        transaction.replace(R.id.main_frame, selectedFragment);
//                        transaction.commit();
//                        return true;
//                    }
//
//
//                });

        public void createLogInScreen () {

            if (mAuth.getCurrentUser() != null) {
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
                changeToMainFrag();

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
                changeToMainFrag();

                showSignedInSnack();
            } else {
                Snackbar.make(findViewById(R.id.main_frame), "Log in failed, try again later",
                        Snackbar.LENGTH_LONG)
                        .show();
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.

            }
        }
    }

    //En snackbar som vissar vem som är inloggad
    private void showSignedInSnack() {
       Snackbar.make(findViewById(R.id.main_frame), getString(R.string.logged_in_as) + FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),
               Snackbar.LENGTH_SHORT)
               .show();
    }

    private void changeToMainFrag() {
        //creates fragment
        mainFragment = new MainFragment();
        //fragment transaction
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.main_frame, mainFragment);
        fragmentTransaction.commit();
    }

    public FirebaseFirestore getDb() {
        return db;
    }
}
