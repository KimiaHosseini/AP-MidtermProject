package DiscordFeatures;

import UserFeatures.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class DiscordServer {
    private String name;
    private ArrayList<Channel> chatChannels;
    private User owner;
    private HashMap<User, HashSet<Role>> members;
    private ArrayList<Role> serverRoles;

    public DiscordServer(String name, User owner){
        this.name = name;
        this.owner = owner;
        chatChannels = new ArrayList<>();

        members = new HashMap<>();
        serverRoles = new ArrayList<>();
    }

    public boolean haveThisAccessibility(User user, Permissions permission){
        HashSet<Role> roles = members.get(user);
        for (Role role : roles) {
            if (role.getPermissions().contains(permission))
                return true;
        }
        return false;
    }

    // only members of a discord server can add members
    public void addMember(User user){
        members.put(user, new HashSet<>());
    }

    public void giveRole(User user, Role role){
        HashSet<Role> roles = members.get(user);
        roles.add(role);
        members.replace(user, roles);
    }
}
