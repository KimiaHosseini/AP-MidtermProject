package DiscordFeatures;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

public class Role implements Serializable {
    @Serial
    private static final long serialVersionUID = 5043264932957705945L;
    private final String name;
    private final HashSet<Permissions> permissions;

    /**
     * Creates new role with given name and permissions
     * @param name String name of role
     * @param permissions HashSet of permissions this role has
     */
    public Role(String name, HashSet<Permissions> permissions) {
        this.name = name;
        this.permissions = permissions;
    }

    /**
     * returns name of this role
     * @return String name of role
     */
    public String getName() {
        return name;
    }

    /**
     * returns all the permissions this role has
     * @return HashSet<Permission> </Permission> permissions
     */
    public HashSet<Permissions> getPermissions() {
        return permissions;
    }

    /**
     * creates a new role named owner with all the permissions
     * @return owner role
     */
    public static Role allPermissions() {
        Permissions[] arr = {Permissions.CHANNEL_PRIVACY, Permissions.ADD_CHANNEL, Permissions.CREATE_ROLES,
                Permissions.CHANGE_SERVER_NAME, Permissions.REMOVE_CHANNEL, Permissions.DELETE_SERVER,
                Permissions.PIN_MESSAGES, Permissions.REMOVE_MEMBER, Permissions.ASSIGN_ROLE};

        HashSet<Permissions> ownerPermissions = new HashSet<>(Arrays.asList(arr));
        return new Role("owner", ownerPermissions);
    }

    /**
     * creates a new role named member with no permissions
     * @return member role
     */
    public static Role memberRole() {
        return new Role("member", new HashSet<>());
    }

    /**
     * Override equals method, changed so that any two roles that have equal
     * names and permissions are deemed equal
     *
     * @param o Role in question
     * @return whether the two roles are equal or not
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return name.equals(role.name) && permissions.equals(role.permissions);
    }

    /**
     * necessary method created with equals method
     *
     * @return int hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, permissions);
    }
}
