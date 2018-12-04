package se.shitchat.shitchatapp.activitys;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import se.shitchat.shitchatapp.R;

public class FullscreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_fullscreen);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_fullscreen);


        ImageView im = findViewById(R.id.fullscreen_imageView);
        String url = getIntent().getStringExtra("image");

        //test for null exception
        if (url == null || im == null) {
                finish();
        }


        //load image
        Picasso.get().load(url).placeholder(R.drawable.ic_panorama_black_24dp).into(im);

        //download image
        im.setOnClickListener(v -> test((ImageView) (v)));
    }

    /*private void downloadImage(ImageView v) {
        //to get the image from the ImageView (say iv)
        BitmapDrawable draw = (BitmapDrawable) v.getDrawable();
        Bitmap bitmap = draw.getBitmap();

        FileOutputStream outStream = null;
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard.getAbsolutePath() + "/SuperChat");
        dir.mkdirs();
        String fileName = String.format("%d.jpg", System.currentTimeMillis());
        File outFile = new File(dir, fileName);
        outStream = new FileOutputStream(outFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
        outStream.flush();
        outStream.close();
    }*/
    private void test(ImageView v) {
        mDownloadAndSave();
    }


    private void downlodImage(ImageView v) {
        // Get the image from drawable resource as drawable object
        Drawable drawable = v.getDrawable();

        // Get the bitmap from drawable object
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();




        // Save image to gallery
        String savedImageURL = MediaStore.Images.Media.insertImage(
                getContentResolver(),
                bitmap,
                "Bird",
                "Image of bird");

        Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);

        Uri uri = Uri.parse(savedImageURL);
        mediaScanIntent.setData(uri);
        sendBroadcast(mediaScanIntent);
    }

    public void mDownloadAndSave() {
        // Setting up file to write the image to.
        File f = new File("/mnt/sdcard/img.png");

        // Open InputStream to download the image.
        InputStream is;
        try {
            is = new URL("http://www.tmonews.com/wp-content/uploads/2012/10/androidfigure.jpg").openStream();

            // Set up OutputStream to write data into image file.
            OutputStream os = new FileOutputStream(f);

            CopyStream(is, os);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);

        //Uri uri = Uri.parse(savedImageURL);
        //mediaScanIntent.setData(uri);
        //sendBroadcast(mediaScanIntent);
    }

    public static void CopyStream(InputStream is, OutputStream os) {
        final int buffer_size = 1024;
        try {
            byte[] bytes = new byte[buffer_size];
            for (;;) {
                int count = is.read(bytes, 0, buffer_size);
                if (count == -1)
                    break;
                os.write(bytes, 0, count);
            }
        } catch (Exception ex) {

        }
    }
}
