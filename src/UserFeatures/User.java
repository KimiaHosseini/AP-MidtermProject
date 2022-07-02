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

/**
 * Each user contains all the data of each client. This data contains the login information, as well as
 * all the places the user is communicating in, such as private chats and servers. The users are saved and
 * read from file and all other data is accessible from user.
 */
public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = -7980400529655280743L;
    private final String username;
    private String password;
    private final String email;
    private final String phoneNumber;
    private File pfp;
    private Status status;

    private ArrayList<User> friendRequests = new ArrayList<>();
    private ArrayList<User> friends = new ArrayList<>();
    private HashSet<User> blockedUsers = new HashSet<>();

    private ArrayList<PrivateChat> privateChats = new ArrayList<>();
    private ArrayList<DiscordServer> servers;

    private Chat currentChat;

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

    /**
     * @param serverIndex given server index
     * @return server in this user servers with given server index
     */
    public DiscordServer getServer(int serverIndex) {
        if (serverIndex > servers.size())
            return null;
        return servers.get(serverIndex - 1);
    }

    /**
     * removes given server from list of servers of this user
     * @param server given server
     */
    public void removeServer(DiscordServer server) {
        servers.remove(server);
    }

    /**
     * set password to new given password
     * @param password given password String
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @param server given server
     * @return whether the given server is in this users list of servers or not
     */
    public boolean isInServer(DiscordServer server) {
        return servers.contains(server);
    }

    /**
     * sets current chat to null
     */
    public void setChatToNull() {
        currentChat = null;
    }

    /**
     * sets current chat equal to given chat
     * @param currentChat given chat
     */
    public void setCurrentChat(Chat currentChat) {
        this.currentChat = currentChat;
    }

    /**
     * @param chat given chat
     * @return whether the user is currently in the given chat or not,
     * whether the currentChat is equal to this chat or not
     */
    public boolean isInThisChat(Chat chat) {
        if (this.currentChat == null || chat == null)
            return false;
        return chat.equals(this.currentChat);
    }

    /**
     * @param username username
     * @return whether the user is currently in the private chat with user of this username or not
     */
    public boolean isInThisChat(String username) {
        PrivateChat privateChat = doesPrivateChatExist(username);
        if (privateChat == null)
            return false;
        return privateChat.equals(currentChat);
    }

    /**
     * adds private chat to list of private chats of this user
     * @param privateChat given private chat
     */
    public void addPrivateChat(PrivateChat privateChat) {
        privateChats.add(privateChat);
    }

    /**
     * @param username given username
     * @return boolean whether the user has a private chat with the user of this username or not
     */
    public PrivateChat doesPrivateChatExist(String username) {
        for (PrivateChat privateChat : privateChats) {
            if (privateChat.getPerson2Username().equals(username) || privateChat.getPerson1Username().equals(username))
                return privateChat;
        }
        return null;
    }

    /**
     * @return String username of the user
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return current status of the user
     */
    public Status getStatus() {
        return status;
    }

    /**
     * sets current status of user with index of given status
     * @param statusIndex int status index
     */
    public void setStatus(int statusIndex) {
        this.status = Status.values()[statusIndex];
    }

    /**
     * sets current status of user with given status
     * @param status given status
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * setter for pfp File
     * @param pfp file
     */
    public void setPfp(File pfp) {
        this.pfp = pfp;
    }

    /**
     * @param password String password
     * @return whether the parameter is equal to the password of the user
     */
    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    /**
     * adds a given user to list of friendRequests
     * @param user given user
     */
    public void addRequest(User user) {
        friendRequests.add(user);
    }

    /**
     * removes request from given user from list of requests
     * @param user given user
     */
    public void removeRequest(User user) {
        friendRequests.remove(user);
    }

    /**
     * finds request from given user and removes from list of requests and adds to list of friends
     * @param user given user
     */
    public void acceptRequest(User user) {
        removeRequest(user);
        if (!friends.contains(user))
            friends.add(user);
        user.addFriend(this);
    }

    /**
     * @param username given username
     * @return User in list of requests with given username
     */
    public User findRequest(String username) {
        for (User user : friendRequests) {
            if (user.getUsername().equals(username))
                return user;
        }
        return null;
    }

    /**
     * adds user from list of friends of this user
     * @param user given user
     */
    public void addFriend(User user) {
        friends.add(user);
    }

    /**
     * adds user from list of blocked users of this user
     * @param user given user
     */
    public void addBlockedUser(User user) {
        blockedUsers.add(user);
    }

    /**
     * removes user from list of blocked users of this user
     * @param user given user
     */
    public void unblock(User user) {
        blockedUsers.remove(user);
    }

    /**
     * @param user given user
     * @return whether given user is in the requested list of this user or not
     */
    public boolean isRequestedAlready(User user) {
        return friendRequests.contains(user);
    }

    /**
     * @param user given user
     * @return whether given user is in the blocked list of this user or not
     */
    public boolean isBlockedAlready(User user) {
        return blockedUsers.contains(user);
    }

    /**
     * @param user given user
     * @return whether given user is in the friends list of this user or not
     */
    public boolean isInFriends(User user) {
        return friends.contains(user);
    }

    /**
     * adds a server to the users arrayList of servers
     * @param discordServer given discordServer to be added
     */
    public void addServer(DiscordServer discordServer) {
        servers.add(discordServer);
    }

    /**
     * @return String of all friend requests of this user
     */
    public String getRequestsListAsString() {
        String s = "";
        if (friendRequests.isEmpty())
            return "Empty\n";
        int i = 1;
        for (User user : friendRequests) {
            s = s.concat(i++ + ") " + user + "\n");
        }
        return s;
    }

    /**
     * @return String of all of this user friends
     */
    public String getFriendsListAsString() {
        String s = "";
        if (friends.isEmpty())
            return "Empty\n";
        for (User user : friends) {
            s = s.concat(user.toStringWithStatus() + "\n");
        }
        return s;
    }

    /**
     * @return String of all privateChats with this user
     */
    public String getPrivateChatsUsernamesListAsString() {
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

    /**
     * @return String of all users blocked by this user
     */
    public String getBlockedListAsString() {
        String s = "";
        if (blockedUsers.isEmpty())
            return "Empty\n";
        for (User user : blockedUsers) {
            s = s.concat(user + "\n");
        }
        return s;
    }

    /**
     * @return String of all servers this user is a member of (numbered)
     */
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

    /**
     * @return String of username
     */
    @Override
    public String toString() {
        return "username: " + username;
    }

    /**
     * @return String of username with added current status of the user
     */
    public String toStringWithStatus() {
        return username + " [" + status + "]";
    }
}