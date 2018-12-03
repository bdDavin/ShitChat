package se.shitchat.shitchatapp.holders;

import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import se.shitchat.shitchatapp.R;

public class ChatsViewHolder extends RecyclerView.ViewHolder {
    public final TextView chatsUsername;
    public final TextView lastMessage;
    public final ImageView profileImage;
    public final ConstraintLayout chatsParent;
    public final TextView date;

    public ChatsViewHolder(View itemView) {
        super(itemView);

        chatsUsername = itemView.findViewById(R.id.chats_username);
        lastMessage = itemView.findViewById(R.id.chats_last_message);
        profileImage = itemView.findViewById(R.id.profile_image);
        chatsParent = itemView.findViewById(R.id.chats_parent);
        date = itemView.findViewById(R.id.chats_date);
    }
}