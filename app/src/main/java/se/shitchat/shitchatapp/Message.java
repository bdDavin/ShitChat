package se.shitchat.shitchatapp;



import com.google.firebase.firestore.ServerTimestamp;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;


public class Message {


    //Fields
    private LocalDateTime time;
    private String creationDate;
    private String message;
    private String uid;
    private String name;
    private @ServerTimestamp Date ServerTime;



    //Constructor (Firebase)
    public Message() {
    }

    public Message(String message) {
        this.message = message;
        this.creationDate = "1337";
        //Get current date time
        time = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
       this.creationDate = time.format(formatter);


    }


    //Getters/Setters

    public String getCreationDate() {


        return creationDate;
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
