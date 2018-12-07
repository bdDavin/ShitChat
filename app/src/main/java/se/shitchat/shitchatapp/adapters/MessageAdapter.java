package se.shitchat.shitchatapp.adapters;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.Objects;
import java.util.UUID;

import dmax.dialog.SpotsDialog;
import se.shitchat.shitchatapp.R;
import se.shitchat.shitchatapp.SaveImageHelper;
import se.shitchat.shitchatapp.activitys.FullscreenActivity;
import se.shitchat.shitchatapp.activitys.MessageActivity;
import se.shitchat.shitchatapp.classes.Message;
import se.shitchat.shitchatapp.holders.MessageHolder;


public class MessageAdapter extends FirestoreRecyclerAdapter<Message, RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ME = 1;
    private static final int VIEW_TYPE_OTHER = 2;
    private static final int PERMISSION_REQUEST_CODE = 1000;
    private MessageActivity activity;



    public MessageAdapter(FirestoreRecyclerOptions options, MessageActivity a) {
        super(options);
        activity = a;
    }


    @Override
    protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull Message model) {

        //displays information
            MessageHolder hold = (MessageHolder) holder;
            hold.messageView.setText(model.getMessage());
            hold.dateView.setText(model.getDisplayTime());
            hold.senderView.setText(model.getName());


        //displays imageview if picture is sent
        if (model.getImage() != null && !Objects.equals(model.getImage(), "default")) {
            Picasso.get().load(model.getImage()).placeholder(R.drawable.ic_panorama_black_24dp).into(hold.pictureView);

            if (hold.pictureView.getDrawable() != null)
            hold.pictureView.setVisibility(View.VISIBLE);
            hold.messageView.setVisibility(View.GONE);
        }
        else {
            hold.pictureView.setVisibility(View.GONE);
            hold.messageView.setVisibility(View.VISIBLE);
        }

        hold.pictureView.setOnClickListener(v -> {

            //show full image
            Intent i = new Intent(v.getContext(), FullscreenActivity.class);
            //image url
            i.putExtra("image", model.getImage());
            v.getContext().startActivity(i);
        });

        hold.pictureView.setOnLongClickListener(view -> {

            //test for permission granted
            if (ActivityCompat.checkSelfPermission(view.getContext().getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(view.getContext(), "You should grant permission", Toast.LENGTH_SHORT).show();

                ActivityCompat.requestPermissions((Activity) (view.getContext()),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MessageActivity.PERMISSION_REQUEST_CODE);

            }
            else {

                //build dialog box
                AlertDialog.Builder download = new AlertDialog.Builder(view.getContext(), R.style.LightDialogTheme);
                download.setMessage("Do you want to download this image?")
                        .setCancelable(false)

                        //on click listener
                        .setPositiveButton("Yes", (dialog, which) -> {

                            //download image

                            //shows message
                            AlertDialog dialog2 = new SpotsDialog(view.getContext());
                            dialog2.show();
                            dialog2.setMessage("Downloading...");


                            Log.i("image", "onBitmapLoaded: 3");
                            //creates unique id
                            String fileName = UUID.randomUUID().toString() + ".jpg";


                            //creates helper object
                            SaveImageHelper helper = new SaveImageHelper(view.getContext(),
                                    dialog2,
                                    view.getContext().getApplicationContext().getContentResolver(),
                                    fileName, "Image Description");
                            //protectedFromGarbageCollectorTargets.add(helper);

                            //upload image
                            Picasso.get().load(model.getImage())
                                    .into(helper);
                            //displays message
                            Toast.makeText(view.getContext(), "Saved image", Toast.LENGTH_SHORT).show();


                        })
                        //onclick listener
                        .setNegativeButton("No", (dialog, which) -> {
                            dialog.cancel();
                        });

                //show download dialog
                AlertDialog downloadDialog = download.create();
                downloadDialog.setTitle("Download Image");
                downloadDialog.show();
            }
            return true;
        });

    }

    //private boolean downloadImage(View view)


    @NonNull
    @Override
    public  RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        RecyclerView.ViewHolder viewHolder = null;

        //creates message based on the sender
        switch (viewType) {
            case VIEW_TYPE_ME:
                View vMe = layoutInflater.inflate(R.layout.message_item, viewGroup, false);
                viewHolder = new MessageHolder(vMe);

                break;
            case VIEW_TYPE_OTHER:
                View vOther = layoutInflater.inflate(R.layout.message_item_recieve, viewGroup, false);
                viewHolder = new MessageHolder(vOther);
                break;


                //if picture is sent
        }
        return viewHolder;
    }

    @Override
    public void onDataChanged() {
        activity.updateSeenStatus();
    }

    @Override
    public int getItemViewType(int position) {
        if (this.getItem(position).getUid().equals(
                Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())) {
            return VIEW_TYPE_ME;
        } else {
            return VIEW_TYPE_OTHER;
        }
    }


}
