package se.shitchat.shitchatapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity {

    private Toolbar t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        t = findViewById(R.id.profileToolbar);
        setSupportActionBar(t);
        TextView userName = findViewById(R.id.username_text);
        ImageView userImage = findViewById(R.id.profile_image);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //displays username
        userName.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        //displays user image
        if(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl() != null ) {
            //TODO load in user image
        }
        else {
            userImage.setImageResource(R.drawable.default_profile);
        }
    }
}
