package se.shitchat.shitchatapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class SearchAdapter extends FirestoreRecyclerAdapter<User, SearchAdapter.SearchHolder> {

    public FirebaseAuth mAuth = FirebaseAuth.getInstance();
    public FirebaseFirestore db = FirebaseFirestore.getInstance();
    public static String groupID;


    public SearchAdapter(@NonNull FirestoreRecyclerOptions<User> options) {

        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull SearchHolder holder, int position, @NonNull User model) {
        
        holder.username.setText(model.getUsername());

        holder.userParent.setOnClickListener(v -> {

            v.getContext();

            String groupId = groupID;

            //adds member to group
            if(groupId != null) {
                groupID = null;
                DocumentReference group = db.collection("groups").document(groupId);

                // Atomically add a new region to the "UserNames" array field.
                group.update("userNames", FieldValue.arrayUnion(model.getUsername()));

                // Atomically add a new user id to the "UserId" array field.
                group.update("userId", FieldValue.arrayUnion(getSnapshots().getSnapshot(position).getId())).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent i = new Intent(v.getContext(), MessageActivity.class);
                        i.putExtra("groupId", groupId);
                        v.getContext().startActivity(i);
                    }
                });

            }
            //creates a new group
            else {
                //creates new group
                String groupName = "default";
                Chat chat = new Chat(groupName);
                String userId = mAuth.getCurrentUser().getUid();
                String userName = mAuth.getCurrentUser().getDisplayName();
                chat.addUser(userName, userId);

                //add clicked user to group
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
            }

        });
    }



    @NonNull
    @Override
    public SearchHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.search_item, viewGroup, false);

        return new SearchHolder(v);
    }

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
