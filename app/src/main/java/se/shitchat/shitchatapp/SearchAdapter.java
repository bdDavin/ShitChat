package se.shitchat.shitchatapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class SearchAdapter extends FirestoreRecyclerAdapter<User, SearchAdapter.SearchHolder> {


    public SearchAdapter(@NonNull FirestoreRecyclerOptions<User> options) {
        super(options);
    }

    private static void onUserClick(View view) {
        Intent i = new Intent(view.getContext(), MessageActivity.class);

        view.getContext().startActivity(i);

    }

    @Override
    protected void onBindViewHolder(@NonNull SearchHolder holder, int position, @NonNull User model) {

        holder.username.setText(model.getUsername());
        holder.username.setOnClickListener(SearchAdapter::onUserClick);

    }

    @NonNull
    @Override
    public SearchHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.search_item,viewGroup, false);

        return new SearchHolder(v);
    }

    class SearchHolder extends RecyclerView.ViewHolder {

        TextView username;

        public SearchHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.userUsername);
        }
    }

}
