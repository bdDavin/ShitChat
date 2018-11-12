package se.shitchat.shitchatapp;

import com.google.firebase.firestore.auth.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class Message {


    //Fields
    private LocalDateTime creationDate;
    private String message;
    private String uid;
    private String name;



    //Constructor (Firebase)
    public Message() {
    }

    public Message(String message) {
        this.message = message;
        //Get current date time
         creationDate = LocalDateTime.now();

    }


    //Getters/Setters

    public String getCreationDate() {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String formatDateTime = creationDate.format(formatter);

        return formatDateTime;
    }

    public String getMessage() {
        return message;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setUserID(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }
}
