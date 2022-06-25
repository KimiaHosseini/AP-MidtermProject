package UserFeatures;

import java.io.File;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

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

    public User(String username, String password, String email, String phoneNumber) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.status = Status.OFFLINE;
    }

    public String getUsername() {
        return username;
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

    public User getRequester(int index){
        return requests.get(index);
    }

    public int getRequestsNum(){
        return requests.size();
    }

    public boolean isRequestedAlready(User user){
        return requests.contains(user);
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
    @Override
    public String toString() {
        return "username: " + username;
    }

    public String toStringWithStatus(){
        return username + " [" + status + "]";
    }
}