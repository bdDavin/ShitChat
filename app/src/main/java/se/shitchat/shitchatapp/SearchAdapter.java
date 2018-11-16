package se.shitchat.shitchatapp;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private List<User> userList;
    private ViewGroup parent;

    public SearchAdapter(List<User> userList) {

        this.userList = userList;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_layout, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

         viewHolder.userName.setText(userList.get(userList.size()).getUsername());


    }

    @Override
    public int getItemCount() {
        return  0;

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        View view;

        public TextView userName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            view = itemView;

            userName = view.findViewById(R.id.name_text);

        }
    }

}
