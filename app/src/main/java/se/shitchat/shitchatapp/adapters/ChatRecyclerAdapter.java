package se.shitchat.shitchatapp.adapters;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

import se.shitchat.shitchatapp.R;
import se.shitchat.shitchatapp.activitys.MessageActivity;
import se.shitchat.shitchatapp.classes.Chat;
import se.shitchat.shitchatapp.holders.ChatsViewHolder;

public class ChatRecyclerAdapter extends FirestoreRecyclerAdapter<Chat, ChatsViewHolder> {

    private Context context;
    private RecyclerView recyclerView;

    public ChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<Chat> options, Context c, RecyclerView rv) {
        super(options);
        context = c;
        recyclerView = rv;
    }


    @Override
    protected void onBindViewHolder(@NonNull ChatsViewHolder holder, int position, @NonNull Chat chatModel) {
         FirebaseAuth mAuth = FirebaseAuth.getInstance();
         FirebaseFirestore db = FirebaseFirestore.getInstance();

        //sätter datan till namnet
        if (!chatModel.getName().equals("default")) {
            holder.chatsUsername.setText(chatModel.getName());
        } else {

            //ändrar namn till medlemmarna
            ArrayList<String> names = chatModel.getUserNames();
            StringBuilder namesFormat = new StringBuilder();
            for (int i = 0; i < names.size(); i++) {
                if (!names.get(i).equalsIgnoreCase(mAuth.getCurrentUser().getDisplayName())) {
                    namesFormat.append(names.get(i)).append(" ");
                }
            }
            holder.chatsUsername.setText(namesFormat.toString());
        }
        String imageUrl = chatModel.getImage();
        String groupId = getSnapshots().getSnapshot(position).getId();

        //displays image
        if (imageUrl == null || imageUrl.equals("default")) {
            ArrayList<String> ids = chatModel.getUserId();

            //displays user profile if only two members
            if (ids.size() == 2) {
                for (int i = 0; i < ids.size(); i++) {
                    if (!ids.get(i).equals(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()))
                        db.collection("users")
                                .document(ids.get(i))
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {

                                    //get image from firebase
                                    String friendURL = documentSnapshot.getString("image");
                                    if (friendURL == null || friendURL.equals("default")) {
                                        holder.profileImage.setImageResource(R.drawable.default_profile);
                                    } else {
                                        Picasso.get().load(friendURL).placeholder(R.drawable.default_profile).into(holder.profileImage);
                                    }
                                });
                }
            } else {
                holder.profileImage.setImageResource(R.drawable.default_profile);
            }
        } else {
            Picasso.get().load(imageUrl).placeholder(R.drawable.default_profile).into(holder.profileImage);
        }
        //frågar databasen efter det senaste meddelandet i gruppen och sätter det i vyn
        db.collection("groups")
                .document(groupId)
                .collection("messages")
                .orderBy("creationDate", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener((snapshot, e) -> {
                    if (!Objects.requireNonNull(snapshot).isEmpty()) {
                        String m = snapshot.getDocuments().get(0).getString("message");
                        holder.lastMessage.setText(m);
                        holder.date.setText(chatModel.getLastUpdated());
                    }
                });
        //sätter en onClick på alla items så när man klickar öppnas meddelandeaktivitetn
        //och skickar med grupp dokumentets namn
        holder.chatsParent.setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), MessageActivity.class);
            i.putExtra("groupId", groupId);
            i.putExtra("groupName", holder.chatsUsername.getText());
            v.getContext().startActivity(i);
            Activity a = (Activity) v.getContext();
            a.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        db.collection("groups")
                .document(groupId)
                .get()
                .addOnCompleteListener(task -> {
                    DocumentSnapshot document = task.getResult();
                    String answer = document.getString(mAuth.getCurrentUser().getUid()+"HasSeen");
                    if ("false".equals(answer)){
                        holder.chatsUsername.setTypeface(null, Typeface.BOLD);
                        holder.lastMessage.setTypeface(null, Typeface.BOLD);
                        holder.date.setTypeface(null, Typeface.BOLD);
                        holder.chatsUsername.setTextColor(context.getColor(R.color.colorPrimary));
                        holder.lastMessage.setTextColor(context.getColor(R.color.colorPrimary));
                        holder.date.setTextColor(context.getColor(R.color.colorPrimary));
                    }
                });
        Log.d("hej", "position "+ position);
        Log.d("hej", "item "+holder.chatsUsername.getText());
        Log.d("hej", "seen "+holder.chatsUsername.getCurrentTextColor());
    }

    @NonNull
    @Override
    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_chats, viewGroup, false);
        return new ChatsViewHolder(view);
    }

    @Override
    public void onDataChanged() {
        Objects.requireNonNull(recyclerView.getLayoutManager()).smoothScrollToPosition(recyclerView, null,0);
    }
}

