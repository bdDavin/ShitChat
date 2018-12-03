package se.shitchat.shitchatapp.holders;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import se.shitchat.shitchatapp.R;

public class MessageHolder extends RecyclerView.ViewHolder {

    //Fields
    public final TextView dateView;
    public final TextView messageView;
    public final ImageView pictureView;
    public final TextView senderView;

    public MessageHolder(@NonNull View itemView) {
        super(itemView);

        //gets all references
        messageView = itemView.findViewById(R.id.text_view_message);
        dateView = itemView.findViewById(R.id.text_view_time);
        pictureView = itemView.findViewById(R.id.imageView);
        senderView = itemView.findViewById(R.id.text_view_sender);

    }




}
