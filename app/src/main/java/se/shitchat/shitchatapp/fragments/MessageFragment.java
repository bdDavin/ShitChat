package se.shitchat.shitchatapp.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.firestore.FirebaseFirestore;

import se.shitchat.shitchatapp.MainActivity;
import se.shitchat.shitchatapp.Message;
import se.shitchat.shitchatapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MessageFragment extends Fragment {

    private Button guessButton;
    private FirebaseFirestore db;


    public MessageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_message, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set up guess button
        guessButton = getActivity().findViewById(R.id.send_button);
        guessButton.setOnClickListener((View v) -> guessButtonPressed(v));

    }

    private void guessButtonPressed(View view) {
        //get input
        String input = userInput();

        //import db
        MainActivity main = (MainActivity) getActivity();
        db = main.getDb();

        //create message
        Message message = new Message();

        //send message
        sendMessage(message, db);
    }

    //TODO getinput from keyboard
    private String userInput() {
        return "hej";
    }

    //TODO send message to firestore
    private void sendMessage(Message message, FirebaseFirestore db ) {

    }
}
