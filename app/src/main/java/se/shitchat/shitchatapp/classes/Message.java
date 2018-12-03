package se.shitchat.shitchatapp.classes;



import com.google.firebase.firestore.ServerTimestamp;


import java.text.SimpleDateFormat;
import java.util.Date;


public class Message {



    //Fields
    private String message;
    private String uid;
    private String name;

    @ServerTimestamp
    private Date timeStamp;
    private String creationDate;
    private String image;


    //Constructor (Firebase)
    public Message() {
    }


    //Getters/Setters
    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreationDate() {
        return creationDate;
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

    public void setCreationDate() {
        if (this.creationDate == null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            this.creationDate = (dateFormat.format(date)); //2016/11/16 12:08:43
        }
    }

    public String getDisplayTime() {
        //only displays HH:mm
        return creationDate.substring(11, 16);
    }

    public String getImage() {
        return this.image;
    }


    public void setImage(String image) {
        this.image = image;
    }
}
