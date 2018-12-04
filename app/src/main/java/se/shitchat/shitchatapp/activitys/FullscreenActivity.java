package se.shitchat.shitchatapp.activitys;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import se.shitchat.shitchatapp.R;

public class FullscreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        ImageView im = findViewById(R.id.fullscreen_imageView);

        String url = getIntent().getStringExtra("image");

        if (url != null) {
            Picasso.get().load(url).placeholder(R.drawable.ic_panorama_black_24dp).into(im);
        }
    }
}
