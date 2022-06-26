package DiscordFeatures;

import DiscordFeatures.Permissions;

import java.util.HashSet;

public class Role {

    private String name;
    private HashSet<Permissions> permissions;

    public Role(String name, HashSet<Permissions> permissions) {
        this.name = name;
        this.permissions = permissions;
    }

    public HashSet<Permissions> getPermissions() {
        return permissions;
    }
}
