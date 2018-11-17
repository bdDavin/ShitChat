package se.shitchat.shitchatapp;




import com.google.firebase.firestore.ServerTimestamp;


import java.time.format.DateTimeFormatter;
import java.util.Date;


public class Message {



    //Fields
    //private LocalDateTime time;
    //private String creationDate;
    private String message;
    private String uid;
    private String name;
    private @ServerTimestamp Date timeStamp;



    //Constructor (Firebase)
    public Message() {
    }


    //Getters/Setters
    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreationDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        //formatter.format(serverTime);
        return "2";
    }

    public Date getTimestamp() {
        return timeStamp;
    }

    public void setTimestamp(Date timeStamp) {
        this.timeStamp = timeStamp;
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
