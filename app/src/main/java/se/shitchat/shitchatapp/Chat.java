package se.shitchat.shitchatapp;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Innehåller all info för en chat
 */
public class Chat {

    private String name;
    private String image;

    private ArrayList messages;
    private ArrayList<String> userId;
    private ArrayList<String> userNames;

    public String getLastUpdated() {
        return lastUpdated;
    }

    private String lastUpdated;
    public Chat(){        image = "default";
    }

    public Chat(String name) {
        this.name = name;
        userId = new ArrayList<>();
        userNames = new ArrayList<>();
        image = "default";

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        this.lastUpdated = (dateFormat.format(date));
    }

    public Chat(String name, ArrayList messages, ArrayList<String> users) {
        this.name = name;
        this.messages = messages;
        this.userId = users;
        image = "default";

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public void setMessages(ArrayList<String> messages) {
        this.messages = messages;
    }


    private void addUserId(String userId) {
        this.userId.add(userId);
    }

    private void addUserName(String userName) {
        this.userNames.add(userName);
    }

    public void addUser(String userName, String userID) {
       addUserId(userID);
       addUserName(userName);
    }

    public ArrayList<String> getUserId() {
        return userId;
    }

    public ArrayList<String> getUserNames() {
        return userNames;
    }


    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
