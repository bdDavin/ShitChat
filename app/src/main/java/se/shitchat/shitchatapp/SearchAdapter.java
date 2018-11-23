package se.shitchat.shitchatapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class SearchAdapter extends FirestoreRecyclerAdapter<User, SearchAdapter.SearchHolder> {

    public FirebaseAuth mAuth = FirebaseAuth.getInstance();
    public FirebaseFirestore db = FirebaseFirestore.getInstance();


    public SearchAdapter(@NonNull FirestoreRecyclerOptions<User> options) {

        super(options);
    }

    //Adds user to item
    @Override
    protected void onBindViewHolder(@NonNull SearchHolder holder, int position, @NonNull User model) {

        holder.username.setText(model.getUsername());

        chooseUser(holder, position, model);

    }

    //Adds chosen user to new chat activity
    private void chooseUser(@NonNull SearchHolder holder, int position, @NonNull User model) {

        holder.userParent.setOnClickListener(v -> {
            String groupName = "default";
            Chat chat = new Chat(groupName);
            String userId = mAuth.getCurrentUser().getUid();
            String userName = mAuth.getCurrentUser().getDisplayName();
            chat.addUser(userName, userId);

            String friendId = getSnapshots().getSnapshot(position).getId();
            String friendname = model.getUsername();
            chat.addUser(friendname, friendId);

            db.collection("groups").add(chat).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                @Override
                public void onComplete(@NonNull Task<DocumentReference> task) {
                    DocumentReference document = task.getResult();
                    String newId = document.getId();
                    Intent i = new Intent(v.getContext(), MessageActivity.class);
                    i.putExtra("groupId", newId);
                    v.getContext().startActivity(i);
                }

            });

        });

    }


    @NonNull
    @Override
    public SearchHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.search_item, viewGroup, false);

        return new SearchHolder(v);
    }

    //Setup recycler view items
    class SearchHolder extends RecyclerView.ViewHolder {

        TextView username;
        View userParent;

        public SearchHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.userUsername);
            userParent = itemView.findViewById(R.id.user_parent);
        }

    }

}
