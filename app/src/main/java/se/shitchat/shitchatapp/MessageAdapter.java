package se.shitchat.shitchatapp;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;


public class MessageAdapter extends FirestoreRecyclerAdapter<Message, MessageHolder> {


    public MessageAdapter(FirestoreRecyclerOptions options) {
        super(options);
    }


    @Override
    protected void onBindViewHolder(MessageHolder holder, int position, Message model) {

        holder.messageView.setText(model.getMessage());
        holder.dateView.setText(model.getCreationDate());
        //TODo change to user image
        holder.pictureView.setImageResource(R.drawable.default_profile);

    }

    @NonNull
    @Override
    public MessageHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.message_item, viewGroup, false);
        return new MessageHolder(v);
    }
}
