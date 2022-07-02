package DiscordFeatures;

import UserFeatures.User;

import java.io.Serializable;
import java.util.*;

/**
 * Each Discord server contains a list of users that are members of the server, as well
 * a list of users that are banned from this server. The server also has a name with a
 * list of channels in which the users can view and send messages in. Each member or user
 * of the DiscordServer has a set of roles that determine their permissions in the server
 */

public class DiscordServer implements Serializable {
    private String name;
    private ArrayList<Channel> channels;
    private HashMap<User, HashSet<Role>> members;
    private ArrayList<Role> serverRoles;
    private ArrayList<User> bannedUsers;

    /**
     * creates new discord server with initial name as well as member with role owner
     * that has created the server. This method also initializes the lists necessary
     * for this server in the fields.
     *
     * @param name  String name of server
     * @param owner User that created the server
     */
    public DiscordServer(String name, User owner) {
        this.name = name;
        channels = new ArrayList<>();
        channels.add(new Channel("general"));
        addToAllChannels(owner);
        members = new HashMap<>();
        HashSet<Role> ownerPerms = new HashSet<Role>();
        ownerPerms.add(Role.allPermissions());
        members.put(owner, ownerPerms);
        serverRoles = new ArrayList<>();
        addRole(Role.allPermissions());
        bannedUsers = new ArrayList<>();
    }

    /**
     * sets name of Discord server
     *
     * @param name String name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return returns String name of server
     */
    public String getName() {
        return name;
    }

    /**
     * returns all users of this server, without the mapped roles to them
     *
     * @return ArrayList of users that are members of this server
     */
    public ArrayList<User> getMembers() {
        return new ArrayList<>(members.keySet());
    }

    /**
     * returns role of specific index in list of serverRoles
     *
     * @param roleIndex given role index
     * @return Role at that index in the list
     */
    public Role getRole(int roleIndex) {
        return serverRoles.get(roleIndex - 1);
    }

    /**
     * @return returns most recently added channel to the arrayList of channels on this server
     */
    public Channel getRecentlyAddedChannel() {
        return channels.get(channels.size() - 1);
    }

    /**
     * adds user to the members of this server and gives member
     * role to the new user
     *
     * @param user given user being added to the server
     */
    private void addUser(User user) {
        HashSet<Role> roles = new HashSet<>();
        roles.add(Role.memberRole());
        addRole(Role.memberRole());
        members.put(user, roles);
    }

    /**
     * creates and adds new channel to the channel arrayList of the server
     *
     * @param name name of the new channel
     */
    public void addChannel(String name) {
        Channel newChannel = new Channel(name);
        newChannel.setUsers(getMembers());
        channels.add(newChannel);
    }

    /**
     * adds role to the arrayList of roles this server has
     * if role is not previously there
     *
     * @param role role to be added
     */
    public void addRole(Role role) {
        for (Role temp : serverRoles) {
            if (temp.equals(role))
                return;
        }
        serverRoles.add(role);
    }

    /**
     * adds given user to all channels on the server
     *
     * @param user given user
     */
    private void addToAllChannels(User user) {
        for (Channel channel : channels) {
            channel.addUser(user);
        }
    }

    /**
     * adds user to the server by adding the user to list of members
     * as well as adding the user to all the channels on the server
     *
     * @param user given user
     */
    public void addMember(User user) {
        addUser(user);
        addToAllChannels(user);
    }

    /**
     * if the given channel index is valid, removes the
     * channel of that index from the arraylist of channels on the server
     *
     * @param channel
     */
    public void deleteChannel(int channel) {
        if (channel > channels.size()) {
            return;
        }
        channels.remove(channel - 1);
    }

    /**
     * @param channelIndex given channel index
     * @return returns the channel of the given index
     */
    public Channel getChannel(int channelIndex) {
        if (channelIndex > channels.size()) {
            return null;
        }
        return channels.get(channelIndex - 1);
    }

    /**
     * bans a member from the server by removing it from all channels as well as
     * removing the user from members and adding the user to the list of banned users
     *
     * @param username username of banned user
     * @return whether the username was valid and the banning was successful
     */
    public boolean banMember(String username) {
        User chosenUser = getUser(username);
        if (!removeMember(username))
            return false;
        removeFromAllChannels(chosenUser);
        bannedUsers.add(chosenUser);
        return true;
    }

    /**
     * removes member from server by removing the member from the Hashmap of
     * members as well as deleting the Roles mapped to it
     *
     * @param username given username of the user we want to delete
     * @return whether removing the member was successful or not, based on whether the username was valid or not
     */
    public boolean removeMember(String username) {
        User chosenUser = getUser(username);
        if (chosenUser == null)
            return false;
        members.remove(chosenUser);
        removeFromAllChannels(chosenUser);
        return true;
    }

    /**
     * removes given user from all channels on the server
     *
     * @param user
     */
    public void removeFromAllChannels(User user) {
        for (Channel channel : channels) {
            try {
                channel.removeUser(user);
            } catch (NoSuchElementException e) {
            }
        }
    }

    /**
     * checks if user is banned from server and is on
     * list of banned users of this server
     *
     * @param username String username of given user
     * @return whether the user is banned or not
     */
    public boolean isBanned(String username) {
        for (User user : bannedUsers) {
            if (user.getUsername().equals(username))
                return true;
        }
        return false;
    }


    /**
     * assigns a specific given role to a user with the given username
     *
     * @param username given username
     * @param role     given role
     * @return whether adding the role to the roles of the member was successful or not
     */
    public boolean assignRole(String username, Role role) {
        Set<User> users = members.keySet();
        User chosenUser = null;
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                chosenUser = user;
                break;
            }
        }

        if (chosenUser == null) {
            return false;
        }
        HashSet<Role> newRoles = members.get(chosenUser);
        newRoles.add(role);

        members.replace(chosenUser, members.get(chosenUser), newRoles);
        return true;
    }

    /**
     * deletes a role from a members hashset of roles
     *
     * @param username username of given user
     * @param roleName name of role to be deleted
     * @return whether the role was deleted successfully or not
     */
    public boolean deleteMemberRole(String username, String roleName) {
        User chosenUser = getUser(username);
        Role chosenRole = null;
        HashSet<Role> newRoles = members.get(chosenUser);
        for (Role role : newRoles) {
            if (role.getName().equals(roleName))
                chosenRole = role;
        }
        if (chosenRole == null)
            return false;

        newRoles.remove(chosenRole);
        members.replace(chosenUser, members.get(chosenUser), newRoles);
        return true;
    }

    /**
     * @param username given username
     * @return all roles of the member with the given username on this server
     */
    public HashSet<Role> getMemberRoles(String username) {
        Set<User> users = members.keySet();
        User chosenUser = null;
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                chosenUser = user;
                break;
            }
        }
        if (chosenUser == null) {
            return null;
        }
        return members.get(chosenUser);
    }

    /**
     * removes a user from the list of bannedUsers on this server
     *
     * @param userIndex index of given user
     * @return whether the removing was successful or not
     */
    public boolean removeBannedUser(int userIndex) {
        if (userIndex > bannedUsers.size()) {
            return false;
        }
        bannedUsers.remove(userIndex - 1);
        return true;
    }

    /**
     * searches for User in this server with given username
     *
     * @param username given username
     * @return user with given username in this server
     */
    public User getUser(String username) {
        Set<User> users = members.keySet();
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    /**
     * checks whether a member of this server has a certain permission in their roles
     *
     * @param user       given user
     * @param permission given permission
     * @return whether the user has this specific accessibility or not
     */
    public boolean haveThisAccessibility(User user, Permissions permission) {
        if (serverRoles.isEmpty()) {
            return false;
        }

        HashSet<Role> roles = members.get(user);
        for (Role role : roles) {
            if (role.getPermissions().contains(permission))
                return true;
        }
        return false;
    }


    /**
     * returns string of all banned users on list of banned users on this server
     *
     * @return String of banned users
     */
    public String getBannedMembersString() {
        String s = "";
        if (bannedUsers.isEmpty())
            return "Empty\n";

        for (int i = 0; i < bannedUsers.size(); i++) {
            s = s.concat("[" + (i + 1) + "]" + bannedUsers.get(i).getUsername() + "\n");
        }

        return s;
    }

    /**
     * returns string of all members of this server, the keys of the members on this server
     * without the mapped roles
     *
     * @return String of members
     */
    public String getServerMembersString() {
        String s = "";
        Set<User> users = members.keySet();

        for (User user : users) {
            s = s.concat(user.getUsername() + "[" + user.getStatus().toString() + "]" + "\n");
        }
        return s;
    }

    /**
     * returns all roles created on this server and in the arrayList of serverRoles
     *
     * @return String of roles
     */
    public String getServerRolesString() {
        String s = "";
        if (serverRoles.isEmpty())
            return "Empty\n";

        for (int i = 0; i < serverRoles.size(); i++) {
            if (serverRoles.get(i) != null) {
                s = s.concat("[" + (i + 1) + "]" + serverRoles.get(i).getName() + "\n");
            }
        }

        return s;
    }

    /**
     * returns all channels created on this server and in the arrayList of channels that the user is a member in
     *
     * @param user given user
     * @return String of channels
     */
    public String getChannelsToString(User user) {
        String s = "";
        if (channels.isEmpty())
            return "Empty\n";

        for (int i = 0; i < channels.size(); i++) {
            if (channels.get(i) != null && channels.get(i).containsUser(user)) {

                s = s.concat("[" + (i + 1) + "]" + channels.get(i).getName() + "\n");
            }
        }

        return s;
    }

}
