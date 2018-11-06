package se.shitchat.shitchatapp;

import com.google.firebase.firestore.auth.User;

import java.util.Date;

public class Message {

    //fields
    private Date creationDate;
    private String message;
    private String id;
    //TODO add user that created message

    //constructor
    public Message() {
    }

    //getters/settters
    public Date getCreationDate() {
        return creationDate;
    }

    public String getMessage() {
        return message;
    }

    public String getId() {
        return id;
    }
}
