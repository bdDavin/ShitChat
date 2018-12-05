package se.shitchat.shitchatapp.adapters;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import se.shitchat.shitchatapp.activitys.FullscreenActivity;
import se.shitchat.shitchatapp.activitys.ProfileActivity;
import se.shitchat.shitchatapp.classes.Message;
import se.shitchat.shitchatapp.holders.MessageHolder;
import se.shitchat.shitchatapp.R;


public class MessageAdapter extends FirestoreRecyclerAdapter<Message, RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ME = 1;
    private static final int VIEW_TYPE_OTHER = 2;




    public MessageAdapter(FirestoreRecyclerOptions options) {
        super(options);
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

    }


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
    public int getItemViewType(int position) {
        if (this.getItem(position).getUid().equals(
                Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())) {
            return VIEW_TYPE_ME;
        } else {
            return VIEW_TYPE_OTHER;
        }
    }


}
