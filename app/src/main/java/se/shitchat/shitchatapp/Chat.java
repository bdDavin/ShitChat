package se.shitchat.shitchatapp;


import java.util.ArrayList;

/**
 * Innehåller all info för en chat
 */
public class Chat {

    private String name;
    private ArrayList messages;
    private ArrayList users;

    public Chat(){}

    public Chat(String name, ArrayList messages, ArrayList users) {
        this.name = name;
        this.messages = messages;
        this.users = users;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<String> messages) {
        this.messages = messages;
    }

    public ArrayList getUsers() {
        return users;
    }

    public void setUsers(ArrayList<String> users) {
        this.users = users;
    }
}
