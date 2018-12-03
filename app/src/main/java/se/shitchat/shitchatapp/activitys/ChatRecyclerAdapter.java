package se.shitchat.shitchatapp.activitys;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.auth.viewmodel.AuthViewModelBase;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import se.shitchat.shitchatapp.Chat;
import se.shitchat.shitchatapp.R;
import se.shitchat.shitchatapp.holders.ChatsViewHolder;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class ChatRecyclerAdapter extends FirestoreRecyclerAdapter<Chat, ChatsViewHolder> {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public ChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<Chat> options) {
        super(options);
    }


    @SuppressLint("RestrictedApi")
    @Override
    protected void onBindViewHolder(@NonNull ChatsViewHolder holder, int position, @NonNull Chat chatModel) {

        //sätter datan till namnet
        if (!chatModel.getName().equals("default")) {
            holder.chatsUsername.setText(chatModel.getName());
        } else {

            //ändrar namn till medlemmarna
            ArrayList<String> names = chatModel.getUserNames();
            String namesFormat = "";
            for (int i = 0; i < names.size(); i++) {
                if (!names.get(i).equalsIgnoreCase(mAuth.getCurrentUser().getDisplayName())) {
                    namesFormat = namesFormat + names.get(i) + " ";
                }
            }
            holder.chatsUsername.setText(namesFormat);
        }
        String imageUrl = chatModel.getImage();
        String groupId = getSnapshots().getSnapshot(position).getId();

        //displays image
        if (imageUrl == null || imageUrl.equals("default")) {
            ArrayList<String> ids = chatModel.getUserId();

            //displays user profile if only two members
            if (ids.size() == 2) {
                for (int i = 0; i < ids.size(); i++) {
                    if (!ids.get(i).equals(mAuth.getCurrentUser().getUid()))
                        db.collection("users")
                                .document(ids.get(i))
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {

                                    //get image from firebase
                                    String friendURL = documentSnapshot.getString("image");
                                    if (friendURL == null || friendURL.equals("default")) {
                                        holder.profileImage.setImageResource(R.drawable.default_profile);
                                    } else {
                                        Picasso.get().load(friendURL).into(holder.profileImage);
                                    }
                                });
                }
            } else {
                holder.profileImage.setImageResource(R.drawable.default_profile);
            }
        } else {
            Picasso.get().load(imageUrl).into(holder.profileImage);
        }
        //frågar databasen efter det senaste meddelandet i gruppen och sätter det i vyn
        db.collection("groups")
                .document(groupId)
                .collection("messages")
                .orderBy("creationDate", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener((snapshot, e) -> {
                    if (!snapshot.isEmpty()) {
                        String m = snapshot.getDocuments().get(0).getString("message");
                        holder.lastMessage.setText(m);
                        holder.date.setText(chatModel.getLastUpdated());
                    }
                });
        //sätter en onClick på alla items så när man klickar öppnas meddelandeaktivitetn
        //och skickar med grupp dokumentets namn
        holder.chatsParent.setOnClickListener(v -> {
            @SuppressLint("RestrictedApi") Intent i = new Intent(getApplicationContext(), MessageActivity.class);
            i.putExtra("groupId", groupId);
            i.putExtra("groupName", holder.chatsUsername.getText());
            v.getContext().startActivity(i);
            //overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    @NonNull
    @Override
    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_chats, viewGroup, false);
        return new ChatsViewHolder(view);
    }
}

