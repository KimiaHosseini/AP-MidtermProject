package DiscordFeatures;

import UserFeatures.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class DiscordServer {
    private String name;
    private ArrayList<Channel> channels;
    //private User owner;
    private HashMap<User, HashSet<Role>> members;
    //private ArrayList<Role> serverRoles;

    public DiscordServer(String name, User owner){
        this.name = name;
        //this.owner = owner;
        channels = new ArrayList<>();
        channels.add(new Channel("general"));
        members = new HashMap<>();
        HashSet ownerPerms = new HashSet();
        ownerPerms.add(Role.allPermissions());
        members.put(owner, ownerPerms);
        //serverRoles = new ArrayList<>();
    }

    public String getName() {
        return name;
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

    public void allMembersToString() {
        String s = "";
        Set<User> users = members.keySet();
        for (User user : users) {
            s = s.concat(user.toStringWithStatus());
        }
    }


    public void giveRole(User user, Role role){
        HashSet<Role> roles = members.get(user);
        roles.add(role);
        members.replace(user, roles);
    }
}