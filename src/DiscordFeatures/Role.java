package DiscordFeatures;

import DiscordFeatures.Permissions;

import java.util.Arrays;
import java.util.HashSet;

public class Role {

    private String name;
    private HashSet<Permissions> permissions;

    public Role(String name, HashSet<Permissions> permissions) {
        this.name = name;
        this.permissions = permissions;
    }

    public static Role allPermissions() {
        Permissions arr[] = {Permissions.CHANNEL_PRIVACY, Permissions.ADD_CHANNEL, Permissions.CREATE_ROLES,
                Permissions.CHANGE_SERVER_NAME, Permissions.REMOVE_CHANNEL, Permissions.DELETE_SERVER,
                Permissions.PIN_MESSAGES, Permissions.REMOVE_MEMBER, Permissions.VIEW_CHAT_HISTORY, };

        HashSet ownerPermissions = new HashSet(Arrays.asList(arr));
        Role ownerRole = new Role("owner", ownerPermissions);

        return ownerRole;
    }

    public HashSet<Permissions> getPermissions() {
        return permissions;
    }
}
