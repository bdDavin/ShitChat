package se.shitchat.shitchatapp.activitys;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import dmax.dialog.SpotsDialog;
import se.shitchat.shitchatapp.R;
import se.shitchat.shitchatapp.SaveImageHelper;

public class FullscreenActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1000;
    ImageView im;
    String url;
    private SaveImageHelper helper;
    final Set<Target> protectedFromGarbageCollectorTargets = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_fullscreen);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_fullscreen);


        ImageView im = findViewById(R.id.fullscreen_imageView);
        url = getIntent().getStringExtra("image");
        Log.i("image", "onCreate: " +url);

        //test for null exception
        if (url == null || im == null) {
                finish();
        }

        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{

                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, PERMISSION_REQUEST_CODE);
        }


        //load image
        Picasso.get().load(url).placeholder(R.drawable.ic_panorama_black_24dp).into(im);

        //download image
        im.setOnClickListener(v -> imageDownload(v));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode)
        {
            case PERMISSION_REQUEST_CODE:
            {
                if (grantResults.length > 0 && grantResults[0]== PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "PERMISSION GRANTED", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(this, "PERMISSION DENIED", Toast.LENGTH_SHORT).show();
                }
            }
            break;
        }
    }


    private void imageDownload(View v) {

        //test for permission granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "You should grant permission", Toast.LENGTH_SHORT).show();
            requestPermissions(new String[]{

                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, PERMISSION_REQUEST_CODE);
            return;
        }
        else {

            //shows message
            AlertDialog dialog = new SpotsDialog(FullscreenActivity.this);
            dialog.show();
            dialog.setMessage("Downloading...");


            Log.i("image", "onBitmapLoaded: 3");
            //creates unique id
            String fileName = UUID.randomUUID().toString() + ".jpg";


            //creates helper object
            helper = new SaveImageHelper(getBaseContext(),
                    dialog,
                    getApplicationContext().getContentResolver(),
                    fileName, "Image Description" );
            //protectedFromGarbageCollectorTargets.add(helper);

            //upload image
            Picasso.get().load(url)
                   .into(helper);
            //displays message
            Toast.makeText(this, "Saved image", Toast.LENGTH_SHORT).show();




        }


    }

}
