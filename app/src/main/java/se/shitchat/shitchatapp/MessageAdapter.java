package se.shitchat.shitchatapp;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;



public class MessageAdapter extends FirestoreRecyclerAdapter<Message, RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ME = 1;
    private static final int VIEW_TYPE_OTHER = 2;




    public MessageAdapter(FirestoreRecyclerOptions options) {
        super(options);
    }


    @Override
    protected void onBindViewHolder(RecyclerView.ViewHolder holder, int position, Message model) {


            //if Im the sender
        if (model.getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

            //sets information
            MessageHolder hold = (MessageHolder) holder;
            hold.messageView.setText(model.getMessage());
            hold.dateView.setText(model.getCreationDate());
            hold.senderView.setText(model.getName());

            //TODo change to user image
            //hold.pictureView.setImageResource(R.drawable.default_profile);
        }
        else {
            MessageHolder hold = (MessageHolder) holder;
            hold.messageView.setText(model.getMessage());
            hold.dateView.setText(model.getCreationDate());
            hold.senderView.setText(model.getName());

            //TODo change to user image
            //hold.pictureView.setImageResource(R.drawable.default_profile);
        }

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
        }
        return viewHolder;
    }



    @Override
    public int getItemViewType(int position) {
        if (this.getItem(position).getUid().equals(
                FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            return VIEW_TYPE_ME;
        } else {
            return VIEW_TYPE_OTHER;
        }
    }


}
