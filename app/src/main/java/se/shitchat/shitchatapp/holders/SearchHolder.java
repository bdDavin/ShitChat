package se.shitchat.shitchatapp.holders;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import se.shitchat.shitchatapp.R;

public class SearchHolder extends RecyclerView.ViewHolder {

    public final TextView username;
    public final View userParent;
    public final ImageView userImage;

    public SearchHolder(@NonNull View itemView) {
        super(itemView);
        username = itemView.findViewById(R.id.userUsername);
        userParent = itemView.findViewById(R.id.user_parent);
        userImage = itemView.findViewById(R.id.userProfileImage);
    }
}
