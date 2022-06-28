package DiscordFeatures;

import UserFeatures.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class DiscordServer implements Serializable {
    private String name;
    private ArrayList<Channel> channels;
    //private User owner;
    private HashMap<User, HashSet<Role>> members;
    private ArrayList<Role> serverRoles;

    public DiscordServer(String name, User owner){
        this.name = name;
        channels = new ArrayList<>();
        channels.add(new Channel("general"));
        addToAllChannels(owner);
        members = new HashMap<>();
        HashSet ownerPerms = new HashSet();
        ownerPerms.add(Role.allPermissions());
        members.put(owner, ownerPerms);
        serverRoles = new ArrayList<>();
        serverRoles.add(Role.allPermissions());
    }


    public void setName(String name) {
        this.name = name;
    }

    private void addUser(User user) {
        HashSet<Role> roles = new HashSet<>();
        roles.add(Role.memberRole());
        serverRoles.add(Role.memberRole());
        members.put(user, roles);
    }

    public void addChannel(Channel channel) {
        channels.add(channel);
    }
    public Channel getRecentlyAddedChannel() {
        return channels.get(channels.size()-1);
    }

    public void addChannel(String name) {
        Channel newChannel = new Channel(name);
        newChannel.setUsers(getMembers());
        channels.add(newChannel);
    }

    public ArrayList<User> getMembers() {
        return new ArrayList<User>(members.keySet());
    }

    public ArrayList<Channel> getChannels() {
        return channels;
    }

    public String getName() {
        return name;
    }

    public String getChannelsToString() {
        String s = "";
        if (channels.isEmpty())
            return "Empty\n";

        for (int i = 0; i < channels.size(); i++) {
            if (channels.get(i) != null) {

                s = s.concat("[" + (i + 1) + "]" + channels.get(i).getName() + "\n");
            }
        }

        return s;
    }

    public void deleteChannel(int channel) {
        if (channel > channels.size()) {
            return;
        }
        channels.remove(channel - 1);
    }

    public Channel getChannel(int channelIndex) {
        if (channelIndex > channels.size()) {
            return null;
        }
        return channels.get(channelIndex - 1);
    }

    private void addToAllChannels(User user) {
        for (Channel channel : channels) {
            channel.addUser(user);
        }
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
        addUser(user);
        addToAllChannels(user);
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