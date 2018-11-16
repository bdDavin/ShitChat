package se.shitchat.shitchatapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

// Create the basic adapter extending from RecyclerView.Adapter
// Note that we specify the custom ViewHolder which gives us access to our views
public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder> {

    private ArrayList<String> groupNames;
    private ArrayList<String> lastGroupMessage;

    private Context mcontext;

    public ChatsAdapter(ArrayList<String> groups, ArrayList<String> message, Context mcontext) {
        this.groupNames = groups;
        this.lastGroupMessage = message;
        this.mcontext = mcontext;
        Log.d("Hej", "ChatsAdapter: "+ lastGroupMessage.toString());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_chats, viewGroup, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        viewHolder.chatsUsername.setText(groupNames.get(i));
        viewHolder.lastMessage.setText(lastGroupMessage.get(i));

        viewHolder.chatsParent.setOnClickListener(v -> {
            //Visa den chatten man klickat på.

        });


    }

    @Override
    public int getItemCount() {
        return groupNames.size();
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView chatsUsername;
        public TextView lastMessage;
        public LinearLayout chatsParent;


        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            chatsUsername = itemView.findViewById(R.id.chats_username);
            lastMessage = itemView.findViewById(R.id.chats_last_message);
            chatsParent = itemView.findViewById(R.id.chats_parent);

        }
    }
}