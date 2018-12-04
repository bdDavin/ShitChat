package se.shitchat.shitchatapp.adapters;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.Objects;
import com.squareup.picasso.Picasso;

import se.shitchat.shitchatapp.classes.Chat;
import se.shitchat.shitchatapp.activitys.MessageActivity;
import se.shitchat.shitchatapp.R;
import se.shitchat.shitchatapp.classes.User;
import se.shitchat.shitchatapp.holders.SearchHolder;

public class SearchAdapter extends FirestoreRecyclerAdapter<User, SearchHolder> {

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    public static String groupID;

    public SearchAdapter(@NonNull FirestoreRecyclerOptions<User> options) {

        super(options);
    }

    //Adds user to item
    @Override
    protected void onBindViewHolder(@NonNull SearchHolder holder, int position, @NonNull User model) {

        holder.username.setText(model.getUsername());

        String userUrl = model.getImage();
        if (userUrl == null || userUrl.equals("default")) {
            holder.userImage.setImageResource(R.drawable.default_profile);
        } else {
            Picasso.get().load(userUrl).into(holder.userImage);
        }

        chooseUser(holder, position, model);

    }

    //Adds chosen user to new chat activity
    private void chooseUser(@NonNull SearchHolder holder, int position, @NonNull User model) {

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
                group.update("userId", FieldValue.arrayUnion(getSnapshots().getSnapshot(position).getId())).addOnCompleteListener(task -> {
                    Intent i = new Intent(v.getContext(), MessageActivity.class);
                    i.putExtra("groupId", groupId);
                    v.getContext().startActivity(i);
                });

            }

            else {
                //creates new group
                String groupName = "default";
                Chat chat = new Chat(groupName);
                String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                String userName = mAuth.getCurrentUser().getDisplayName();
                chat.addUser(userName, userId);

                //add clicked user to group
                String friendId = getSnapshots().getSnapshot(position).getId();
                String friendname = model.getUsername();
                chat.addUser(friendname, friendId);


                db.collection("groups").add(chat).addOnCompleteListener(task -> {
                    DocumentReference document = task.getResult();
                    String newId = Objects.requireNonNull(document).getId();
                    Intent i = new Intent(v.getContext(), MessageActivity.class);
                    i.putExtra("groupId", newId);
                    v.getContext().startActivity(i);
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

}
