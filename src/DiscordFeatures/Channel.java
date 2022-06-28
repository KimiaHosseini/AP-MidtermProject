package DiscordFeatures;

import UserFeatures.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Channel extends Chat implements Serializable {
    private String name;
    private ArrayList<Message> messages;
    private ArrayList<Message> pinnedMessages;
    private ArrayList<User> users;

    public Channel(String name){
        this.name = name;
        messages = new ArrayList<>();
        pinnedMessages = new ArrayList<>();
        users = new ArrayList<>();
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    public void addUser(User user) {
        users.add(user);
    }

    public String getName() {
        return name;
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    public String getMessages() {
        String s ="";
        if (messages.isEmpty())
            return "Empty";
        for (Message message : messages) {
            s = s.concat(message.toString() + "\n");
        }
        return s;
    }

    public void pinMessage(Message message){
        pinnedMessages.add(message);
    }
}
