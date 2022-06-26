package DiscordFeatures;

import UserFeatures.User;

import java.util.ArrayList;
public class Channel {
    private String name;
    private ArrayList<Message> messages;
    private ArrayList<Message> pinnedMessages;
    private ArrayList<User> bannedUsers;

    public Channel(String name){
        this.name = name;
        messages = new ArrayList<>();
        pinnedMessages = new ArrayList<>();
        bannedUsers = new ArrayList<>();
    }

    public void pinMessage(Message message){
        pinnedMessages.add(message);
    }
}
