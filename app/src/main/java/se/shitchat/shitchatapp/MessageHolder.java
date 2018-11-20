package se.shitchat.shitchatapp;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

class MessageHolder extends RecyclerView.ViewHolder {

    //Fields
    public TextView dateView;
    public final TextView messageView;
    public ImageView pictureView;
    public TextView senderView;

    public MessageHolder(@NonNull View itemView) {
        super(itemView);

        //gets all references
        messageView = itemView.findViewById(R.id.text_view_message);
        dateView = itemView.findViewById(R.id.text_view_time);
        pictureView = itemView.findViewById(R.id.imageView);
        senderView = itemView.findViewById(R.id.text_view_sender);

        //TODO show buble on active
        //Glide.with(context).load(GIF_URI).into(new GlideDrawableImageViewTarget(IMAGE_VIEW));

    }




}
