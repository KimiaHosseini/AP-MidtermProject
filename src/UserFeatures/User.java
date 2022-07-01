package UserFeatures;

import DiscordFeatures.Channel;
import DiscordFeatures.Chat;
import DiscordFeatures.DiscordServer;
import DiscordFeatures.PrivateChat;

import java.io.File;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = -7980400529655280743L;
    private String username;
    private String password;
    private String email;
    private String phoneNumber;
    private Status status;
    private ArrayList<User> requests = new ArrayList<>();
    private ArrayList<User> friends = new ArrayList<>();
    private File pfp;
    private HashSet<User> blockedUsers = new HashSet<>();
    private ArrayList<PrivateChat> privateChats = new ArrayList<>();
    private Chat currentChat;
    private ArrayList<DiscordServer> servers;


    public User(String username, String password, String email, String phoneNumber) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.status = Status.OFFLINE;
        servers = new ArrayList<>();
    }

    public Chat getCurrentChat() {
        return currentChat;
    }

    public DiscordServer getServer(int serverIndex) {
        return servers.get(serverIndex - 1);
    }

    public void removeServer(DiscordServer server ) {
        servers.remove(server);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isInServer(DiscordServer server) {
        if (servers.contains(server)) {
            return true;
        }
        return false;
    }


    public void setChatToNull() {
        currentChat = null;
    }


    public void setCurrentChat(Chat currentChat) {
        this.currentChat = currentChat;
    }

    public boolean isInThisChat(Chat chat){
        return chat.equals(this.currentChat);
    }

    public boolean isInThisChat(String username){
        PrivateChat privateChat = doesPrivateChatExist(username);
        if (privateChat == null)
            return false;
        return privateChat.equals(currentChat);
    }

    public void addPrivateChat(PrivateChat privateChat){
        privateChats.add(privateChat);
    }

    public PrivateChat doesPrivateChatExist(String username){
        for (PrivateChat privateChat : privateChats) {
            if (privateChat.getPerson2Username().equals(username) || privateChat.getPerson1Username().equals(username))
                return privateChat;
        }
        return null;
    }
    public String getUsername() {
        return username;
    }

    public User getFriend(String username) {
        for (User user : friends) {
            if (user.getUsername().equals(username))
                return user;
        }

        return null;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(int statusIndex) {
        this.status = Status.values()[statusIndex];
    }

    public void setStatus(Status status){
        this.status = status;
    }
    public void setPfp(File pfp) {this.pfp = pfp;}

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    public void addRequest(User user){
        requests.add(user);
    }

    public void removeRequest(User user){
        requests.remove(user);
    }

    public void acceptRequest(User user){
        removeRequest(user);
        friends.add(user);
        user.addFriend(this);
    }

    public User findRequest(String username){
        for (User user : requests) {
            if (user.getUsername().equals(username))
                return user;
        }
        return null;
    }
    public void addFriend(User user){
        friends.add(user);
    }

    public void addBlockedUser(User user){
        blockedUsers.add(user);
    }

    public void unblock(User user){
        blockedUsers.remove(user);
    }
    public User getRequester(int index){
        return requests.get(index);
    }

    public int getRequestsNum(){
        return requests.size();
    }

    public boolean isRequestedAlready(User user){
        return requests.contains(user);
    }

    public boolean isBlockedAlready(User user){
        return blockedUsers.contains(user);
    }

    public boolean isInFriends(User user){
        return friends.contains(user);
    }


    public String getRequestsListAsString(){
        String s = "";
        if (requests.isEmpty())
            return "Empty\n";
        int i = 1;
        for (User user : requests) {
            s = s.concat(i++ + ") " + user + "\n");
        }
        return s;
    }

    public String getFriendsListAsString(){
        String s = "";
        if (friends.isEmpty())
            return "Empty\n";
        for (User user : friends) {
            s = s.concat(user.toStringWithStatus() + "\n");
        }
        return s;
    }

    public String getPrivateChatsUsernamesListAsString(){
        String s = "";
        if (privateChats.isEmpty())
            return "Empty\n";
        for (PrivateChat privateChat : privateChats) {
            String temp1 = privateChat.getPerson2Username();
            if (temp1.equals(username))
                temp1 = privateChat.getPerson1Username();
            s = s.concat(temp1 + "\n");
        }
        return s;
    }

    public String getBlockedListAsString(){
        String s = "";
        if (blockedUsers.isEmpty())
            return "Empty\n";
        for (User user : blockedUsers) {
            s = s.concat(user + "\n");
        }
        return s;
    }

    public String serversToString() {
        String s = "";
        if (servers.isEmpty())
            return "Empty\n";

        for (int i = 0; i < servers.size(); i++) {
            if (servers.get(i) != null) {

                s = s.concat("[" + (i + 1) + "]" + servers.get(i).getName() + "\n");
            }
        }

        return s;
    }

    public void addServer(DiscordServer discordServer) { servers.add(discordServer);}



    //    public PrivateChat findPrivateChat(String username){
//        for (PrivateChat privateChat : privateChats) {
//            if (privateChat.getPerson2Username().equals(username) )
//        }
//    }
    @Override
    public String toString() {
        return "username: " + username;
    }

    public String toStringWithStatus(){
        return username + " [" + status + "]";
    }
}