package se.shitchat.shitchatapp;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;

/**
 * Innehåller all info för en chat
 */
public class Chat {

    private String name;

    private ArrayList messages;
    private ArrayList<String> userId;
    private ArrayList<String> userNames;

    public String getLastUpdated() {
        return lastUpdated;
    }

    private String lastUpdated;
    public Chat(){}

    public Chat(String name) {
        this.name = name;
        userId = new ArrayList<>();
        userNames = new ArrayList<>();

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        this.lastUpdated = (dateFormat.format(date));
    }

    public Chat(String name, ArrayList messages, ArrayList<String> users) {
        this.name = name;
        this.messages = messages;
        this.userId = users;
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


}
