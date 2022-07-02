package Client;

import DiscordFeatures.*;
import Handler.InputHandler;
import Handler.MenuHandler;
import Handler.RequestStatus;
import Handler.ResponseStatus;
import Model.Request;
import Model.RequestType;
import Model.Response;
import UserFeatures.Status;
import UserFeatures.User;

import java.io.*;
import java.net.Socket;
import java.util.HashSet;

/**
 * The class Client is in direct communication with the console. It invokes
 * methods that send requests to the server and receives responses to give
 * back output to those requests.
 */
public class Client {
    private final Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private User user;

    /**
     * Creates new Client and connection with the server socket
     *
     * @param socket give socket
     */
    public Client(Socket socket) {
        this.socket = socket;
        try {
            this.oos = new ObjectOutputStream(socket.getOutputStream());
            this.ois = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            shutDown(ois, oos, socket);
        }
    }

    /**
     * main method that runs on client console, creates connection
     * with server host and port and runs client
     */
    public static void main(String[] args) {
        Socket socket;
        try {
            socket = new Socket("localhost", 8888);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Client client = new Client(socket);
        client.runClient();
    }

    /**
     * closes all connections with the server and shuts down the client
     * also sets the user's status to Offline before disconnection
     *
     * @param in     objectInputStream
     * @param out    objectOutputStream
     * @param socket serverSocket
     */
    private void shutDown(ObjectInputStream in, ObjectOutputStream out, Socket socket) {
        try {
            user.setStatus(Status.OFFLINE);
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * gets response from input Stream from server
     *
     * @return Response from server
     */
    synchronized private Response getResponse() {
        Response response = null;
        try {
            response = (Response) ois.readUnshared();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * Sends request to output Stream
     *
     * @param request request to be sent out to server
     */
    private void sendRequest(Request<?> request) {
        try {
            oos.writeUnshared(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * run method for client.
     * this method shows menus and receives input from user to
     * invoke actions and send requests and continues in loops
     * until the user manually exits
     */
    public void runClient() {
        System.out.println("\t".repeat(5) + "Welcome to Discord!");
        while (!socket.isClosed()) {

            System.out.println(MenuHandler.welcomeMenu());
            String menuAction = InputHandler.getString("Enter choice: ");
            switch (menuAction) {
                case "1" -> signUp();
                case "2" -> {
                    if (!logIn()) {
                        break;
                    }
                    boolean continueLoop;
                    do {
                        continueLoop = userLoop();
                    } while (continueLoop);
                }
                case "0" -> System.exit(0);
            }
        }
    }

    /**
     * prints servers and allows client to choose which server they want to enter
     * if server is chosen action is taken to invoke method to enter that server
     */
    private void viewServers() {
        sendRequest(new Request<>(RequestType.PRINT_SERVERS));
        System.out.print((String) getResponse().getData());

        String requestedServer = InputHandler.getString("Enter the numbered index of the server you want to enter: ");
        if (requestedServer.equals("0") || requestedServer.equals("")) {
            return;
        }

        enterServer(requestedServer);
    }

    /**
     * method to view the Members and their statuses of the given server
     *
     * @param requestedServer index of given Server
     */
    private void viewMembers(String requestedServer) {
        Request<String> request = new Request<>(RequestType.VIEW_MEMBERS);
        request.addData("serverIndex", requestedServer);
        sendRequest(request);
        System.out.println(getResponse().getData());
    }

    /**
     * Enters the given server, prompting the menu for the client to chose
     * what actions in that server they want to take
     *
     * @param requestedServer given server
     */
    private void enterServer(String requestedServer) {
        do {
            System.out.println(MenuHandler.serverMenu());
            int menuChoice = InputHandler.getInt("Enter choice: ", 4);
            switch (menuChoice) {
                case 1 -> {
                    if (viewChannels(requestedServer))
                        return;
                    enterChannel(requestedServer);
                }
                case 2 -> viewMembers(requestedServer);
                case 3 -> inviteFriendToServer(requestedServer);
                case 4 -> serverSettings(requestedServer);
                case 0 -> {
                    return;
                }
            }
        } while (true);
    }

    /**
     * prints menu for server settings and prompts client to chose what action they want to take
     *
     * @param requestedServer given server
     */
    private void serverSettings(String requestedServer) {
        DiscordServer server = getServer(requestedServer);
        System.out.println(MenuHandler.serverSettings(server, user));
        int menuChoice = InputHandler.getInt("Enter choice :", 9);
        switch (menuChoice) {
            case 1 -> changeServerName(requestedServer);
            case 2 -> createChannel(requestedServer);
            case 3 -> deleteChannel(requestedServer);
            case 4 -> modifyChannelAccess(requestedServer);
            case 5 -> modifyBanned(requestedServer);
            case 6 -> createNewRole(requestedServer);
            case 7 -> assignRoleToMember(requestedServer);
            case 8 -> deleteServer(requestedServer);
            case 9 -> leaveServer(requestedServer);
            case 0 -> {
            }
        }
    }

    /**
     * prompts client to modify who can access what channels whether it is view access or general access
     *
     * @param requestedServer given server
     */
    private void modifyChannelAccess(String requestedServer) {
        viewChannels(requestedServer);
        String channelIndex = InputHandler.getString("Enter index of channel you want to modify : ");
        String modificationChoice = InputHandler.getString("Do you want to [1] Change who can view [2] Change who can message: ");
        switch (modificationChoice) {
            case "1" -> changeView(requestedServer, channelIndex);
            case "2" -> changeMessengers(requestedServer, channelIndex);
        }
    }

    /**
     * changes who can send messages in a channel making it only view for the chosen user
     *
     * @param requestedServer given server
     * @param channelIndex    given channel
     */
    private void changeView(String requestedServer, String channelIndex) {
        String username = InputHandler.getString("Enter the user that you want to only view this channel: ");
        Request<String> viewOnly = new Request<>(RequestType.VIEW_ONLY);
        viewOnly.addData("serverIndex", requestedServer);
        viewOnly.addData("channelIndex", channelIndex);
        viewOnly.addData("username", username);
        sendRequest(viewOnly);
        if (getResponse().getResponseStatus().equals(ResponseStatus.INVALID_STATUS)) {
            System.out.println("Data invalid.");
            return;
        }
        System.out.println("User can no longer send messages in channel.(can only view)");
    }

    /**
     * changes who can view messages in a channel, users that are chosen here can no longer access the channel
     *
     * @param requestedServer given server
     * @param channelIndex    given channel
     */
    private void changeMessengers(String requestedServer, String channelIndex) {
        String username = InputHandler.getString("Enter the user that can no longer view this channel: ");
        Request<String> noAccess = new Request<>(RequestType.REVOKE_ACCESS);
        noAccess.addData("serverIndex", requestedServer);
        noAccess.addData("channelIndex", channelIndex);
        noAccess.addData("username", username);
        sendRequest(noAccess);
        if (getResponse().getResponseStatus().equals(ResponseStatus.INVALID_STATUS)) {
            System.out.println("Data invalid.");
            return;
        }
        System.out.println("User can no longer view this channel");
    }

    /**
     * removes the client from the server and they no longer have access to the server
     * prompted to ensure they want to leave before exiting
     *
     * @param requestedServer given server
     */
    private void leaveServer(String requestedServer) {
        int choice = InputHandler.getInt("Are you sure you want to leave the server? [1]No  [2]Yes, Exit", 2);
        if (choice == 1)
            return;
        Request<String> leaveServer = new Request<>(RequestType.LEAVE_SERVER);
        leaveServer.addData("username", user.getUsername());
        leaveServer.addData("serverIndex", requestedServer);
        sendRequest(leaveServer);
        getResponse();
        System.out.println("You have successfully left the server. Enter 0 to go back.");
    }

    /**
     * prompts client to modify (delete or add) a role to a user
     *
     * @param requestedServer given server
     */
    private void assignRoleToMember(String requestedServer) {
        Request<String> roleRequest = new Request<>(RequestType.GET_SERVER_MEMBERS);
        roleRequest.addData("serverIndex", requestedServer);
        sendRequest(roleRequest);
        System.out.println((String) getResponse().getData());
        String chosenMember = InputHandler.getString("Enter the username of the members roles you want to modify:");
        if (chosenMember.equals("0")) {
            return;
        }
        int roleChoice = InputHandler.getInt("Do you want to delete or give a role? [1] delete [2] add", 3);
        switch (roleChoice) {
            case 1 -> deleteRole(requestedServer, chosenMember);
            case 2 -> giveRoleToMember(requestedServer, chosenMember);
        }
    }

    /**
     * prompts client to choose a role to assign to a server member of their choice
     *
     * @param requestedServer given server
     * @param chosenMember    user of clients choice
     */
    private void giveRoleToMember(String requestedServer, String chosenMember) {
        Request<String> roleRequest = new Request<>(RequestType.GET_ROLES);
        roleRequest.addData("serverIndex", requestedServer);
        sendRequest(roleRequest);
        String roles = (String) getResponse().getData();
        System.out.println(roles);
        String getRole;
        if (roles.equals("Empty\n")) {
            return;
        } else {
            getRole = InputHandler.getString("Enter the number of the role you want to give to a member: ");
            if (getRole.equals("0"))
                return;
        }
        Request<String> addRoleToMember = new Request<>(RequestType.ASSIGN_ROLE);
        addRoleToMember.addData("serverIndex", requestedServer);
        addRoleToMember.addData("roleIndex", getRole);
        addRoleToMember.addData("username", chosenMember);
        sendRequest(addRoleToMember);
        getResponse();
        System.out.println("Role successfully assigned.");
    }

    /**
     * prompts client to chose a role to delete from the member of their choice
     *
     * @param requestedServer given server
     * @param chosenMember    user of clients choice
     */
    private void deleteRole(String requestedServer, String chosenMember) {
        Request<String> getMemberRoles = new Request<>(RequestType.GET_MEMBER_ROLES);
        getMemberRoles.addData("serverIndex", requestedServer);
        getMemberRoles.addData("username", chosenMember);
        sendRequest(getMemberRoles);
        Response response = getResponse();
        if (response.getResponseStatus().equals(ResponseStatus.INVALID_USERNAME)) {
            System.out.println("The username you have selected does not exist in this server.");
            return;
        }

        String roles = (String) response.getData();
        System.out.println(roles);
        if (roles.equals("Empty\n"))
            return;

        String getRole = InputHandler.getString("Enter the name of the role you want to delete: ");
        Request<String> deleteRole = new Request<>(RequestType.DELETE_ROLE);
        deleteRole.addData("serverIndex", requestedServer);
        deleteRole.addData("role", getRole);
        deleteRole.addData("username", chosenMember);
        sendRequest(deleteRole);
        if (getResponse().getResponseStatus().equals(ResponseStatus.INVALID_STATUS)) {
            System.out.println("role does not exist.");
        } else {
            System.out.println("role deleted successfully.");
        }
    }

    /**
     * prompts user to chose permissions and name of a new role and adds that role to
     * server roles of given server
     *
     * @param requestedServer given server
     */
    private void createNewRole(String requestedServer) {
        String roleName = InputHandler.getString("Enter new role name: ");
        HashSet<Permissions> perms = setPermissions();
        if (perms == null) {
            return;
        }
        Role role = new Role(roleName, perms);
        Request<Role> newRoleRequest = new Request<>(RequestType.NEW_ROLE);
        newRoleRequest.addData("role", role);
        Role roleServer = new Role(requestedServer, null);
        newRoleRequest.addData("roleServer", roleServer);
        sendRequest(newRoleRequest);
        getResponse();
        System.out.println("Role created.");
    }

    /**
     * prompt for client to select permissions of a new role
     *
     * @return Hash set of permissions the client chose
     */
    public HashSet<Permissions> setPermissions() {
        HashSet<Permissions> perms = new HashSet<>();
        int temp = InputHandler.getInt("Permission to change server name: [1]On [2]Off", 3);
        if (temp == 1) {
            perms.add(Permissions.CHANGE_SERVER_NAME);
        } else if (temp == 0) {
            return null;
        }
        temp = InputHandler.getInt("Permission to Add Channel: [1]On [2]Off", 3);
        if (temp == 1) {
            perms.add(Permissions.ADD_CHANNEL);
        } else if (temp == 0) {
            return null;
        }
        temp = InputHandler.getInt("Permission to Remove Channel: [1]On [2]Off", 3);
        if (temp == 1) {
            perms.add(Permissions.REMOVE_CHANNEL);
        } else if (temp == 0) {
            return null;
        }
        temp = InputHandler.getInt("Permission to Ban Members: [1]On [2]Off", 3);
        if (temp == 1) {
            perms.add(Permissions.REMOVE_MEMBER);
        } else if (temp == 0) {
            return null;
        }
        temp = InputHandler.getInt("Permission to Pin Messages: [1]On [2]Off", 3);
        if (temp == 1) {
            perms.add(Permissions.PIN_MESSAGES);
        } else if (temp == 0) {
            return null;
        }
        temp = InputHandler.getInt("Permission to Change Channel Privacy: [1]On [2]Off", 3);
        if (temp == 1) {
            perms.add(Permissions.CHANNEL_PRIVACY);
        } else if (temp == 0) {
            return null;
        }
        temp = InputHandler.getInt("Permission to view chat history: [1]On [2]Off", 3);
        if (temp == 2) {
            perms.add(Permissions.CANT_VIEW_CHAT_HISTORY);
        } else if (temp == 0) {
            return null;
        }

        return perms;
    }

    /**
     * prompts client to choose whether they want to ban or unban a user from given server
     *
     * @param requestedServer given server
     */
    private void modifyBanned(String requestedServer) {
        int choice = InputHandler.getInt("Do you want to unban or ban a user: [1] ban [2] unban", 3);
        switch (choice) {
            case 1 -> banMember(requestedServer);
            case 2 -> unbanMember(requestedServer);
        }
    }

    /**
     * prompts client to chose which user they want to unban from this server
     *
     * @param requestedServer given server
     */
    private void unbanMember(String requestedServer) {
        Request<String> bannedMembers = new Request<>(RequestType.GET_BANNED_MEMBERS);
        bannedMembers.addData("serverIndex", requestedServer);
        sendRequest(bannedMembers);
        Response responseList = getResponse();
        System.out.println(responseList.getData());

        if (responseList.getData().equals("Empty\n"))
            return;

        Request<String> unbanMember = new Request<>(RequestType.UNBAN_MEMBER);
        unbanMember.addData("serverIndex", requestedServer);
        String userIndex = InputHandler.getString("Enter index of user you want to unban:");
        unbanMember.addData("userIndex", userIndex);
        sendRequest(unbanMember);
        if (getResponse().getResponseStatus().equals(ResponseStatus.INVALID_STATUS)) {
            System.out.println("invalid index.");
            return;
        }
        System.out.println("user successfully unbanned.");
    }

    /**
     * prompts client to chose which user they want to ban from this server
     *
     * @param requestedServer given server
     */
    private void banMember(String requestedServer) {
        Request<String> getMembers = new Request<>(RequestType.GET_SERVER_MEMBERS);
        getMembers.addData("serverIndex", requestedServer);
        sendRequest(getMembers);
        System.out.println((String) getResponse().getData());
        String chosenMember = InputHandler.getString("Enter the username of the member you want to ban");
        if (chosenMember.equals("0")) {
            return;
        }
        Request<String> banMember = new Request<>(RequestType.BAN_MEMBER);
        banMember.addData("username", chosenMember);
        banMember.addData("serverIndex", requestedServer);
        sendRequest(banMember);
        if (getResponse().getResponseStatus().equals(ResponseStatus.INVALID_STATUS)) {
            System.out.println("member does not exist.");
        } else {
            System.out.println("member banned successfully.");
        }
    }

    /**
     * prompts client to ensure whether they want to delete the server or not
     * and sends request that deletes the server for all users
     *
     * @param requestedServer given server
     */
    private void deleteServer(String requestedServer) {
        Request<String> deleteServer = new Request<>(RequestType.DELETE_SERVER);
        deleteServer.addData("serverIndex", requestedServer);
        if (InputHandler.getInt("Are you sure you want to delete the server?\n[1] No\n[2] Yes, delete", 3) == 2) {
            sendRequest(deleteServer);
            if (getResponse().getResponseStatus().equals(ResponseStatus.INVALID_STATUS)) {
                System.out.println("Invalid server index");
                return;
            }
            System.out.println("Successfully deleted server.");
        }
    }

    /**
     * sends request that changes server name for all members of the server
     *
     * @param requestedServer given server
     */
    private void changeServerName(String requestedServer) {
        Request<String> changeName = new Request<>(RequestType.CHANGE_SERVER_NAME);
        changeName.addData("serverIndex", requestedServer);
        String newName = InputHandler.getString("Enter new name for server: ");
        changeName.addData("newName", newName);
        sendRequest(changeName);
        getResponse();
        System.out.println("Successfully changed name.");
    }

    /**
     * prompts user to choose a channel to delete for all members of the given server
     *
     * @param requestedServer given server
     */
    private void deleteChannel(String requestedServer) {
        Request<String> deleteChannel = new Request<>(RequestType.DELETE_CHANNEL);
        deleteChannel.addData("serverIndex", requestedServer);
        viewChannels(requestedServer);
        String channelIndex = InputHandler.getString("Enter index of channel you want to delete : ");
        deleteChannel.addData("channelIndex", channelIndex);
        sendRequest(deleteChannel);
        if (getResponse().getResponseStatus() == ResponseStatus.VALID_STATUS) {
            System.out.println("Successfully deleted channel.");
        } else {
            System.out.println("Invalid channel index");
        }
    }

    /**
     * creates and adds channel to given server that is accessible by all members of the given server
     *
     * @param requestedServer given server
     */
    private void createChannel(String requestedServer) {
        Request<String> newChannel = new Request<>(RequestType.NEW_CHANNEL);
        newChannel.addData("serverIndex", requestedServer);
        String channelName = InputHandler.getString("Enter name of new channel : ");
        newChannel.addData("name", channelName);
        sendRequest(newChannel);
        getResponse();
        System.out.println("Successfully added channel.");
    }

    /**
     * sends request to get all accessible channels to the client in the given server as a String
     *
     * @param requestedServer given server
     * @return String of channels
     */
    private boolean viewChannels(String requestedServer) {
        Request<String> printChannels = new Request<>(RequestType.PRINT_CHANNELS);
        printChannels.addData("requestedServer", requestedServer);
        sendRequest(printChannels);
        Response response = getResponse();
        System.out.print((String) response.getData());

        return response.getData().equals("This server is no longer accessible.\n");
    }

    /**
     * prompts menu for client to choose what actions they want to take in the channel
     * prompts client to choose which channel they want to enter and enters that channel and allows
     * user to send and listen for messages
     *
     * @param requestedServer given server
     */
    private void enterChannel(String requestedServer) {
        String chosenChannel = InputHandler.getString("Enter index of channel you want to enter: ");
        if (chosenChannel.equals("0"))
            return;
        Channel channel;
        while (true) {
            DiscordServer server = getServer(requestedServer);
            System.out.println(MenuHandler.channelMenu(server, user));
            int choice = InputHandler.getInt("Enter choice: ", 7);

            if (choice == 1) {
                if (printChannelMessages(requestedServer, chosenChannel)) {
                    Request<String> getChannel = new Request<>(RequestType.GET_CHANNEL);
                    getChannel.addData("channelIndex", chosenChannel);
                    getChannel.addData("serverIndex", requestedServer);
                    sendRequest(getChannel);
                    Response response = getResponse();
                    if (response.getResponseStatus().equals(ResponseStatus.INVALID_STATUS))
                        return;
                    channel = (Channel) response.getData();
                    this.user.setCurrentChat(channel);
                } else
                    return;
                listenForMessage(channel);
                sendMessage(chosenChannel, requestedServer, RequestType.SEND_MESSAGE_TO_CHANNEL);
            } else if (choice == 2) {
                closeChat();
                sendFile(chosenChannel, requestedServer);
                sendChannelMessage(chosenChannel, requestedServer, this.user.getUsername() + " sends a file.");
            } else if (choice == 3) {
                closeChat();
                downloadFile(chosenChannel, requestedServer);
            } else if (choice == 4) {
                closeChat();
                reactToMessage(requestedServer, chosenChannel);
            } else if (choice == 5) {
                closeChat();
                replyToMessage(requestedServer, chosenChannel);
            } else if (choice == 6) {
                closeChat();
                viewPinnedMessages(requestedServer, chosenChannel);
            } else if (choice == 7) {
                closeChat();
                pinMessage(requestedServer, chosenChannel);
            } else if (choice == 0)
                return;
        }
    }

    /**
     * prompts user to react to a message, prints all messages in the server and allows the
     * client to choose which message they want to react to
     *
     * @param requestedServer given server
     * @param channelIndex    given channel
     */
    private void reactToMessage(String requestedServer, String channelIndex) {
        if (printChannelMessagesNumbered(requestedServer, channelIndex)) {
            String messageIndex = InputHandler.getString("Enter index of which message you want to react to: ");
            if (messageIndex.equals("0"))
                return;
            int react = InputHandler.getInt("What do you want to react with: [1]Like  [2]Dislike  [3]Laugh", 3);
            switch (react) {
                case 1 -> sendChannelMessage(channelIndex, requestedServer, this.user.getUsername() + " reacted to message" + "(" + messageIndex + ") with " + "[LIKE]");
                case 2 -> sendChannelMessage(channelIndex, requestedServer, this.user.getUsername() + " reacted to message" + "(" + messageIndex + ") with " + "[DISLIKE]");
                case 3 -> sendChannelMessage(channelIndex, requestedServer, this.user.getUsername() + " reacted to message" + "(" + messageIndex + ") with " + "[LAUGH]");
                case 4 -> {
                    return;
                }
            }
            System.out.println("Reaction sent.");
        }
    }

    /**
     * prompts user to reply to a message, prints all messages in the server and allows the
     * client to choose which message they want to reply to
     *
     * @param requestedServer given server
     * @param channelIndex    given channel
     */
    private void replyToMessage(String requestedServer, String channelIndex) {
        if (printChannelMessagesNumbered(requestedServer, channelIndex)) {
            String messageIndex = InputHandler.getString("Enter index of which message you want to reply to: ");
            if (messageIndex.equals("0"))
                return;
            String replyMessage = InputHandler.getString("Enter the message of your reply: ");
            sendChannelMessage(channelIndex, requestedServer, this.user.getUsername() + " replying to message" + "(" + messageIndex + ") with: " + replyMessage);
            System.out.println("Reply sent.");
        }
    }

    /**
     * prints all pinned messages of the given channel in the given server
     *
     * @param requestedServer given server
     * @param channelIndex    given channel
     */
    private void viewPinnedMessages(String requestedServer, String channelIndex) {
        Request<String> viewPinned = new Request<>(RequestType.VIEW_PINNED);
        viewPinned.addData("serverIndex", requestedServer);
        viewPinned.addData("channelIndex", channelIndex);
        sendRequest(viewPinned);

        System.out.println(getResponse().getData());
        System.out.println("end of pinned messages.");
    }

    /**
     * prints all messages in the channel and prompts client to choose which
     * message they want to pin and sends request to pin that message
     *
     * @param requestedServer given server
     * @param channelIndex    given channel
     */
    private void pinMessage(String requestedServer, String channelIndex) {
        Request<String> pinMessage = new Request<>(RequestType.PIN_MESSAGE);
        if (printChannelMessagesNumbered(requestedServer, channelIndex)) {
            pinMessage.addData("serverIndex", requestedServer);
            pinMessage.addData("channelIndex", channelIndex);
            String messageIndex = InputHandler.getString("Enter index of which message you want to pin: ");
            if (messageIndex.equals("0"))
                return;
            pinMessage.addData("messageIndex", messageIndex);
            sendRequest(pinMessage);
            if (getResponse().getResponseStatus().equals(ResponseStatus.VALID_STATUS))
                System.out.println("Message pinned successfully.");
            else
                System.out.println("Message index invalid.");
        }
    }

    /**
     * sends request to return get the server of the given serverIndex
     *
     * @param requestedServer given server index
     * @return DiscordServer of the index
     */
    private DiscordServer getServer(String requestedServer) {
        Request<String> getServer = new Request<>(RequestType.GET_SERVER);
        getServer.addData("serverIndex", requestedServer);
        sendRequest(getServer);
        return (DiscordServer) getResponse().getData();
    }

    /**
     * sends request to receive numbered messages in the channel
     *
     * @param requestedServer given server
     * @param chosenChannel   given channel
     * @return String of numbered messages
     */
    private boolean printChannelMessagesNumbered(String requestedServer, String chosenChannel) {
        Request<String> enterChannel = new Request<>(RequestType.GET_NUMBERED_MESSAGES);
        enterChannel.addData("channelIndex", chosenChannel);
        enterChannel.addData("serverIndex", requestedServer);
        sendRequest(enterChannel);
        Response response = getResponse();
        if (response.getResponseStatus() == ResponseStatus.VALID_STATUS) {
            System.out.println(response.getData());
            return true;
        } else {
            System.out.println("Invalid index");
            return false;
        }
    }

    /**
     * sends request to receive messages in the channel
     *
     * @param requestedServer given server
     * @param chosenChannel   given channel
     * @return string of messages
     */
    private boolean printChannelMessages(String requestedServer, String chosenChannel) {
        Request<String> enterChannel = new Request<>(RequestType.ENTER_CHANNEL);
        enterChannel.addData("channelIndex", chosenChannel);
        enterChannel.addData("serverIndex", requestedServer);
        sendRequest(enterChannel);
        Response response = getResponse();
        if (response.getResponseStatus() == ResponseStatus.VALID_STATUS) {
            System.out.println(response.getData());
            return true;
        } else {
            System.out.println("You no longer have access to this server. Enter 0 to go back.");
            return false;
        }
    }

    /**
     * prompts user to invite friend to user and the chosen user is then added to the server
     *
     * @param requestedServer given server
     */
    private void inviteFriendToServer(String requestedServer) {
        printFriends();
        String friendInvite = InputHandler.getString("Enter username of which friend you want to invite: ");
        if (friendInvite.equals("0"))
            return;
        Request<String> serverInviteRequest = new Request<>(RequestType.INVITE_TO_SERVER);
        serverInviteRequest.addData("username", friendInvite);
        serverInviteRequest.addData("serverIndex", requestedServer);
        sendRequest(serverInviteRequest);
        Response response = getResponse();
        if (response.getResponseStatus() == ResponseStatus.INVALID_USERNAME) {
            System.out.println("This user is not your friend.");
            return;
        } else if (response.getResponseStatus() == ResponseStatus.INVALID_STATUS) {
            System.out.println("friend already in server.");
            return;
        } else if (response.getResponseStatus() == ResponseStatus.BANNED_USER) {
            System.out.println("This user is banned from this server.");
            return;
        }

        User invitedFriend = (User) response.getData();
        invitedFriend.addServer(user.getServer(Integer.parseInt(requestedServer)));
        System.out.println("Friend successfully added.");
    }

    /**
     * prompts user to choose what they want to dod. this menu
     * is displayed to any client that has logged in and invokes
     * methods for each corresponding choice
     *
     * @return whether to continue the loop or not
     */
    private boolean userLoop() {
        int menuAction;
        System.out.println(MenuHandler.userStarterMenu());
        menuAction = InputHandler.getInt("Enter Menu Choice: ", 10);

        switch (menuAction) {
            case 1 -> viewServers();
            case 2 -> {
                closeChat();
                printPrivateChatsUsernames();

                //check username to be valid and not blocked
                Request<String> request = new Request<>(RequestType.CHECK_USERNAME_FOR_CHAT);
                String username = InputHandler.getString("username: ");
                request.addData("username", username);
                sendRequest(request);
                Response response = getResponse();
                if (response.getResponseStatus() == ResponseStatus.INVALID_USERNAME)
                    System.out.println("Invalid input");
                else if (response.getResponseStatus() == ResponseStatus.BLOCKED_USERNAME)
                    System.out.println("You can't send message to this user");
                else {
                    while (true) {
                        int choice = InputHandler.getInt("\n[1] Send Message\n[2] Send File\n[3] Download File", 3);
                        //send message
                        if (choice == 1) {
                            //find previous messages
                            request = new Request<>(RequestType.PRIVATE_CHAT_MESSAGES);
                            request.addData("username", username);
                            sendRequest(request);
                            response = getResponse();

                            //set user's current chat
                            request = new Request<>(RequestType.SET_CURRENT_CHAT);
                            request.addData("username", username);
                            sendRequest(request);

                            sendAndReceiveMessage(response, username);
                        } else if (choice == 2) {
                            //close chat because we don't want to listen for messages anymore
                            closeChat();

                            sendFile(username);
                            sendPrivateChatMessage(username, this.user.getUsername() + " sends a file.");
                        } else if (choice == 3) {
                            closeChat();

                            downloadFile(username);
                        } else if (choice == 0)
                            break;
                    }
                }
            }
            case 3 -> printFriends();
            case 4 -> viewFriendRequests();
            case 5 -> sendFriendRequestProcess();
            case 6 -> blockFriendProcess();
            case 7 -> unblockUserProcess();
            case 8 -> createServer();
            case 9 -> userSettings();
            case 0 -> {
                sendRequest(new Request<>(RequestType.SIGN_OUT));
                Response response = getResponse();
                if (response.getResponseStatus() == ResponseStatus.SIGN_OUT_VALID) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * sends request to close the chat and stop listening for new messages
     */
    private void closeChat() {
        Request<String> endRequest = new Request<>(RequestType.CLOSE_THREAD);
        sendRequest(endRequest);
        this.user.setChatToNull();
    }

    /**
     * prompts client to create new server
     */
    private void createServer() {
        String name = InputHandler.getString("Enter the name of your new server:");
        if (name.equals("0"))
            return;

        Request<String> newServerRequest = new Request<>(RequestType.NEW_SERVER);
        newServerRequest.addData("name", name);
        sendRequest(newServerRequest);
        DiscordServer server = (DiscordServer) getResponse().getData();
        user.addServer(server);
        System.out.println("server created successfully.");
    }

    /**
     * sends request to print all privateChat usernames
     */
    private void printPrivateChatsUsernames() {
        Request<String> print = new Request<>(RequestType.PRINT_PRIVATE_CHATS_USERNAMES);
        print.addData("username", user.getUsername());
        sendRequest(print);
        Response response = getResponse();

        System.out.println((String) response.getData());
    }

    /**
     * sends message to private chat with user with given username
     * @param username given username
     * @param requestType give server
     */
    public void sendMessage(String username, RequestType requestType) {
        while (true) {
            Request<String> request = new Request<>(requestType);
            String text = InputHandler.getString("");
            request.addData("message", user.getUsername() + ": " + text);
            request.addData("username", username);
            if (text.equals("0")) {
                Request<String> endRequest = new Request<>(RequestType.CLOSE_CHAT);
                sendRequest(endRequest);
                this.user.setChatToNull();
                return;
            }
            sendRequest(request);
        }
    }

    /**
     * sends message with corresponding requestType
     * @param channelIndex given channel
     * @param serverIndex given server
     * @param requestType requestType determining whether chat is private chat or channel
     */
    public void sendMessage(String channelIndex, String serverIndex, RequestType requestType) {
        while (true) {
            Request<String> request = new Request<>(requestType);
            String text = InputHandler.getString("");
            if (text.equals("0")) {
                Request<String> endRequest = new Request<>(RequestType.CLOSE_CHAT);
                sendRequest(endRequest);
                this.user.setChatToNull();
                return;
            }
            request.addData("message", user.getUsername() + ": " + text);
            request.addData("channelIndex", channelIndex);
            request.addData("serverIndex", serverIndex);
            sendRequest(request);
        }
    }

    /**
     * sends message to user with given username
     * @param username String username of the private chat we want to send a message to
     * @param text String content of the message
     */
    public void sendPrivateChatMessage(String username, String text) {
        Request<String> request = new Request<>(RequestType.SEND_MESSAGE);
        request.addData("message", text);
        request.addData("username", username);
        sendRequest(request);
    }


    /**
     * sends message to given channel in given server
     * @param channelIndex given channel
     * @param serverIndex given server
     * @param text String content of the message
     */
    public void sendChannelMessage(String channelIndex, String serverIndex, String text) {
        Request<String> request = new Request<>(RequestType.SEND_MESSAGE_TO_CHANNEL);
        request.addData("message", text);
        request.addData("channelIndex", channelIndex);
        request.addData("serverIndex", serverIndex);
        sendRequest(request);
    }

    /**
     * listens for message on a separate thread so that the client can still exit the server
     *
     * @param chat chat in which the client will be listening for messages
     */
    public void listenForMessage(Chat chat) {
        if (!this.user.isInThisChat(chat))
            return;
        new Thread(() -> {
            while (socket.isConnected()) {
                Response response = getResponse();
                if (response.getResponseStatus().equals(ResponseStatus.CLOSE_THREAD)) {
                    user.setChatToNull();
                    return;
                }
                Message message = (Message) response.getData();
                String messageContent = message.getContent();
                if (messageContent.substring(messageContent.indexOf(":") + 2).equals("0")) {
                    break;
                }
                if (!messageContent.contains("sends a file.") && !response.getResponseStatus().equals(ResponseStatus.BANNED_USER))
                    System.out.println(messageContent.substring(0, messageContent.indexOf(":")) + " is typing...");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(message);
            }
        }).start();
    }

    /**
     * new thread to download file so that it doesn't disrupt or slow down the program
     * @param file file that wants to be downloaded
     */
    public void downloadFile(File file) {
        new Thread(() -> {
            try {
                Socket socket = new Socket("localhost", 9999);
                FileInputStream fileInputStream = new FileInputStream(file.getAbsoluteFile());
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                String fileName = file.getName();
                byte[] fileNameBytes = fileName.getBytes();
                byte[] fileContentBytes = new byte[(int) file.length()];
                fileInputStream.read(fileContentBytes);

                dataOutputStream.writeInt(fileNameBytes.length);
                dataOutputStream.write(fileNameBytes);

                dataOutputStream.writeInt(fileContentBytes.length);
                dataOutputStream.write(fileContentBytes);

                System.out.println("Download completed.\nYou can see this file in Downloads");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

    }

    /**
     * prompts user to send a file to the chat that the user is currently in
     * @param username username of the private chat
     */
    private void sendFile(String username) {
        String path = InputHandler.getString("path: ");
        File file = new File(path);
        if (!file.exists()) {
            System.out.println("Invalid path");
            return;
        }

        Request<String> addFileRequest = new Request<>(RequestType.ADD_FILE_TO_CHAT);
        addFileRequest.addData("file", path);
        addFileRequest.addData("username", username);
        sendRequest(addFileRequest);
    }

    /**
     * prompts user to send a file to the given channel in the given server
     * @param channelIndex given channel
     * @param serverIndex given server
     */
    private void sendFile(String channelIndex, String serverIndex) {
        String path = InputHandler.getString("path: ");
        File file = new File(path);
        if (!file.exists()) {
            System.out.println("Invalid path");
            return;
        }

        Request<String> addFileRequest = new Request<>(RequestType.ADD_FILE_TO_CHANNEL);
        addFileRequest.addData("file", path);
        addFileRequest.addData("channelIndex", channelIndex);
        addFileRequest.addData("serverIndex", serverIndex);
        sendRequest(addFileRequest);
    }

    /**
     * prompts user to download a file from the chat that the user is currently in
     * @param username username of the private chat
     */
    private void downloadFile(String username) {
        Request<String> fileNamesRequest = new Request<>(RequestType.PRINT_FILE_NAMES);
        fileNamesRequest.addData("username", username);
        sendRequest(fileNamesRequest);
        Response response = getResponse();
        System.out.println(response.getData());
        String fileName = InputHandler.getString("file's name to download : ");

        Request<String> downloadFileRequest = new Request<>(RequestType.DOWNLOAD_FILE);
        downloadFileRequest.addData("fileName", fileName);
        downloadFileRequest.addData("username", username);
        sendRequest(downloadFileRequest);
        response = getResponse();
        if (response.getResponseStatus() == ResponseStatus.INVALID_FILE_NAME) {
            System.out.println("Invalid file name");
            return;
        }
        File file = (File) response.getData();
        downloadFile(file);
    }

    /**
     * prompts user to download a file from the given channel in the given server
     * @param channelIndex given channel
     * @param serverIndex given server
     */
    private void downloadFile(String channelIndex, String serverIndex) {
        Request<String> fileNamesRequest = new Request<>(RequestType.PRINT_FILE_NAMES_IN_CHANNEL);
        fileNamesRequest.addData("serverIndex", serverIndex);
        fileNamesRequest.addData("channelIndex", channelIndex);
        sendRequest(fileNamesRequest);
        Response response = getResponse();
        System.out.println(response.getData());
        String fileName = InputHandler.getString("file's name to download : ");

        Request<String> downloadFileRequest = new Request<>(RequestType.DOWNLOAD_FILE_IN_CHANNEL);
        downloadFileRequest.addData("fileName", fileName);
        downloadFileRequest.addData("channelIndex", channelIndex);
        downloadFileRequest.addData("serverIndex", serverIndex);
        sendRequest(downloadFileRequest);
        response = getResponse();
        if (response.getResponseStatus() == ResponseStatus.INVALID_FILE_NAME) {
            System.out.println("Invalid file name");
            return;
        }
        File file = (File) response.getData();
        downloadFile(file);
    }

    /**
     * sends request to accept friend request to given username
     * @param requester given username
     * @return whether the request was successful or not
     */
    private void acceptFriendRequest(String requester) {
        Request<String> acceptFriendRequest = new Request<>(RequestType.ACCEPT_FRIEND_REQUEST);
        acceptFriendRequest.addData("receiver", user.getUsername());
        acceptFriendRequest.addData("requester", requester);
        sendRequest(acceptFriendRequest);
        Response response = getResponse();
        if (response.getResponseStatus() == ResponseStatus.VALID_ACCEPT_FRIEND_REQUEST)
            System.out.println("Now " + requester + " is your friend");
        else if (response.getResponseStatus() == ResponseStatus.INVALID_ACCEPT_FRIEND_REQUEST)
            System.out.println("Invalid input");
    }

    /**
     * sends request to unblock given username
     * @param unblock given username
     * @return whether the unblocking was successful or not
     */
    private void unblockUser(String unblock) {
        Request<String> unblockUser = new Request<>(RequestType.UNBLOCK_USER);
        unblockUser.addData("unblocked", unblock);
        unblockUser.addData("requester", user.getUsername());
        sendRequest(unblockUser);
        Response response = getResponse();
        if (response.getResponseStatus() == ResponseStatus.VALID_ACCEPT_FRIEND_REQUEST)
            System.out.println(unblock + " is unblocked");
        else if (response.getResponseStatus() == ResponseStatus.INVALID_ACCEPT_FRIEND_REQUEST)
            System.out.println("Invalid input");
    }

    /**
     * sends request to delete friend request to given username
     * @param requester given username
     * @return whether the request was successful or not
     */
    private void deleteFriendRequest(String requester) {
        Request<String> deleteFriendRequest = new Request<>(RequestType.DELETE_FRIEND_REQUEST);
        deleteFriendRequest.addData("receiver", user.getUsername());
        deleteFriendRequest.addData("requester", requester);
        sendRequest(deleteFriendRequest);
        Response response = getResponse();
        if (response.getResponseStatus() == ResponseStatus.VALID_DELETE_FRIEND_REQUEST)
            System.out.println(requester + " is removed from your requests");
        else if (response.getResponseStatus() == ResponseStatus.INVALID_DELETE_FRIEND_REQUEST)
            System.out.println("Invalid input");
    }

    /**
     * sends request to send friend request to given username
     * @param receiver given username
     * @return whether the request was successful or not
     */
    private boolean sendFriendRequest(String receiver) {
        Request<String> request = new Request<>(RequestType.FRIEND_REQUEST);
        request.addData("receiver", receiver);
        request.addData("requester", user.getUsername());
        sendRequest(request);

        Response response = getResponse();
        if (response.getResponseStatus() == ResponseStatus.FRIEND_REQUEST_TO_FRIEND)
            System.out.println("This user is your friend!");
        else if (response.getResponseStatus() == ResponseStatus.INVALID_USERNAME)
            System.out.println("Invalid input");
        else if (response.getResponseStatus() == ResponseStatus.DUPLICATE_FRIEND_REQUEST)
            System.out.println("You have already sent request to this user");
        else if (response.getResponseStatus() == ResponseStatus.VALID_FRIEND_REQUEST) {
            System.out.println("Successfully sent");
            return true;
        }
        return false;
    }

    /**
     * sends request to block given username
     * @param blocked given username
     * @return whether the blocking was successful or not
     */
    private boolean blockFriend(String blocked) {
        Request<String> request = new Request<>(RequestType.BLOCK_FRIEND);
        request.addData("blocked", blocked);
        request.addData("requester", user.getUsername());
        sendRequest(request);
        Response response = getResponse();
        if (response.getResponseStatus() == ResponseStatus.DUPLICATE_BLOCK)
            System.out.println("This user has already been blocked");
        else if (response.getResponseStatus() == ResponseStatus.INVALID_BLOCK)
            System.out.println("Invalid input");
        else if (response.getResponseStatus() == ResponseStatus.VALID_BLOCK) {
            System.out.println("Successfully blocked");
            return true;
        }
        return false;
    }

    /**
     * sends request to check whether the entered username is correct
     * @param username String entered username
     * @return Response to the request
     */
    private Response checkUsername(String username) {
        Request<String> request = new Request<>(RequestType.CHECK_USERNAME);
        request.addData("username", username);
        sendRequest(request);
        return getResponse();
    }

    /**
     * sends request to sign in the client with given information from console and gets response
     * to check the validity of the sign in
     * @param username given username
     * @param pass given pass
     * @param email given email
     * @param phoneNum given phoneNum
     */
    private void completeSignUp(String username, String pass, String email, String phoneNum) {
        makeSignUpRequest(username, pass, email, phoneNum);
        Response response = getResponse();
        if (response.getResponseStatus() == ResponseStatus.SIGNUP_VALID) {
            System.out.println("successfully signed up");
        }
    }

    /**
     * sends request to sign in the client with given information from console
     * @param username given username
     * @param pass given pass
     * @param email given email
     * @param phoneNum given phoneNum
     */
    private void makeSignUpRequest(String username, String pass, String email, String phoneNum) {
        Request<String> request = new Request<>(RequestType.SIGN_UP);
        request.addData("username", username);
        request.addData("password", pass);
        request.addData("email", email);
        request.addData("phoneNum", phoneNum);
        sendRequest(request);
    }

    /**
     * sends request to set the client's status to the given status
     * @param statusIndex given status
     */
    private void setStatus(int statusIndex) {
        Request<String> request = new Request<>(RequestType.SET_STATUS);
        request.addData("status", Integer.toString(statusIndex - 1));
        sendRequest(request);
        Response response = getResponse();
        if (response.getResponseStatus() == ResponseStatus.VALID_STATUS)
            System.out.println("successfully done");
        else if (response.getResponseStatus() == ResponseStatus.INVALID_STATUS)
            System.out.println("invalid input");
    }

    /**
     * sends request to print the clients friends list and prints
     */
    private void printFriends() {
        Request<String> printList = new Request<>(RequestType.PRINT_FRIENDS);
        printList.addData("username", user.getUsername());
        sendRequest(printList);
        Response response = getResponse();
        System.out.println((String) response.getData());
    }

    /**
     * prompts menu for client to choose from user settings
     */
    private void userSettings() {
        do {
            System.out.println(MenuHandler.settingAccountMenu());
            int menuAction = InputHandler.getInt("Enter Menu Choice: ", 3);
            switch (menuAction) {
                case 1 -> {
                    String imagePath = InputHandler.getString("image path: ");
                    if (imagePath.equals("0"))
                        break;
                    setPFP(imagePath);
                }
                case 2 -> {
                    System.out.println(MenuHandler.statusMenu());
                    int statusIndex = InputHandler.getInt("Enter index of chosen status: ", 5);
                    if (statusIndex == 0)
                        break;
                    setStatus(statusIndex);
                }
                //change pass
                case 3 -> {
                    Request<String> request = new Request<>(RequestType.CHANGE_PASSWORD);
                    request.addData("username", this.user.getUsername());
                    String newPass;
                    do {
                        newPass = InputHandler.getInfo("pass");
                    } while (newPass == null);
                    if (newPass.equals("0"))
                        break;
                    request.addData("pass", newPass);

                    sendRequest(request);

                    Response response = getResponse();
                    if (response.getResponseStatus() == ResponseStatus.SAME_PASSWORD)
                        System.out.println("Same password");
                    else if (response.getResponseStatus() == ResponseStatus.VALID_CHANGE_PASSWORD)
                        System.out.println("successfully changed");
                }
                case 0 -> {
                    return;
                }
            }
        } while (true);
    }

    /**
     * prompts user to choose a file as their pfp and sends request to save that file
     * @param imagePath path of file the client submits as their pfp
     */
    private void setPFP(String imagePath) {
        File profilePhoto = new File(imagePath);
        if (!profilePhoto.exists()) {
            System.out.println("Invalid path");
            return;
        }
        Request<String> request = new Request<>(RequestType.PROFILE_PHOTO);
        request.addData("profilePhoto", imagePath);
        sendRequest(request);
    }

    /**
     * prompts user to sign up and sends request with sign up information
     */
    private void signUp() {
        String username, pass, email, phoneNum;

        do {
            username = InputHandler.getInfo("username");
            RequestStatus requestStatus = InputHandler.checkInfo(username);
            if (requestStatus == RequestStatus.INVALID)
                continue;
            if (requestStatus == RequestStatus.BACK) {
                return;
            }

            if (checkUsername(username).getResponseStatus() == ResponseStatus.INVALID_USERNAME) {
                System.out.println("Duplicate username");
            } else {
                break;
            }

        } while (true);

        do {
            pass = InputHandler.getInfo("pass");
            RequestStatus requestStatus = InputHandler.checkInfo(pass);
            if (requestStatus == RequestStatus.VALID)
                break;
            else if (requestStatus == RequestStatus.BACK) {
                return;
            }
        } while (true);

        do {
            email = InputHandler.getInfo("email");
            RequestStatus requestStatus = InputHandler.checkInfo(email);
            if (requestStatus == RequestStatus.VALID)
                break;
            else if (requestStatus == RequestStatus.BACK) {
                return;
            }
        } while (true);

        do {
            phoneNum = InputHandler.getInfo("phoneNum");
            RequestStatus requestStatus = InputHandler.checkInfo(phoneNum);
            if (requestStatus == RequestStatus.VALID)
                break;
            else if (requestStatus == RequestStatus.BACK) {
                return;
            }
        } while (true);

        completeSignUp(username, pass, email, phoneNum);
    }

    /**
     * prompts user to log in and sends request with log in information
     *
     * @return whether the login was successful or not
     */
    private Boolean logIn() {
        do {
            Request<String> request;
            String username, pass;

            username = InputHandler.getString("username: ");
            if (username.equals("0"))
                return false;

            pass = InputHandler.getString("pass: ");
            if (pass.equals("0"))
                return false;

            request = new Request<>(RequestType.SIGN_IN);
            request.addData("username", username);
            request.addData("pass", pass);
            sendRequest(request);

            Response response = getResponse();

            if (response.getResponseStatus() == ResponseStatus.SIGN_IN_INVALID)
                System.out.println("Wrong username or password");
            else if (response.getResponseStatus() == ResponseStatus.SIGN_IN_VALID) {
                this.user = (User) response.getData();
                user.setChatToNull();
                System.out.println("Successfully signed in");
                break;
            }
        } while (true);

        return true;
    }

    /**
     * prompts user to choose a user to send a friends request to and sends a request to do so
     */
    private void sendFriendRequestProcess() {
        do {
            String receiver = InputHandler.getString("username: ");
            if (receiver.equals("0") || sendFriendRequest(receiver))
                return;
        } while (true);
    }

    /**
     * prompts user to choose a user to block and sends a request to do so
     */
    private void blockFriendProcess() {
        printFriends();
        do {
            String blockedUsername = InputHandler.getString("username: ");
            if (blockedUsername.equals("0") || blockFriend(blockedUsername))
                return;
        } while (true);
    }

    /**
     * prompts user to choose a user to unblock and sends request to do so
     */
    private void unblockUserProcess() {
        do {
            Response response = printBlockedUsers();
            if (response.getResponseStatus() == ResponseStatus.EMPTY_BLOCKED_USERS_LIST)
                return;

            String unblock = InputHandler.getString("write a user's username to unblock: ");
            if (unblock.equals("0"))
                return;
            int menuAction = InputHandler.getInt("[1] Unblock\n[0] Exit", 2);

            switch (menuAction) {
                case 1 -> unblockUser(unblock);
                case 0 -> {
                    return;
                }
            }
        } while (true);
    }

    /**
     * sends request to get all users the client has blocked
     *
     * @return Response from request of getting blocked users
     */
    private Response printBlockedUsers() {
        Request<String> printBlocked = new Request<>(RequestType.PRINT_BLOCKED_FRIENDS);
        printBlocked.addData("username", user.getUsername());
        sendRequest(printBlocked);
        Response response = getResponse();
        System.out.println((String) response.getData());
        return response;
    }

    /**
     * sends request to view list of clients friend requests and prompts user to choose
     * which requests they want to delete or accept
     */
    private void viewFriendRequests() {
        do {
            Request<String> printFriendRequests = new Request<>(RequestType.PRINT_FRIEND_REQUESTS);
            printFriendRequests.addData("username", user.getUsername());
            sendRequest(printFriendRequests);
            Response response = getResponse();
            //print requests list
            System.out.print((String) response.getData());
            if (response.getResponseStatus() == ResponseStatus.EMPTY_FRIEND_REQUEST_LIST)
                return;

            String requester = InputHandler.getString("write a request's username to accept or delete: ");

            if (requester.equals("0"))
                return;

            int menuAction = InputHandler.getInt("[1] Accept\n[2] Delete\n[0] Exit", 3);

            switch (menuAction) {
                case 1 -> acceptFriendRequest(requester);
                case 2 -> deleteFriendRequest(requester);
                case 0 -> {
                    return;
                }
            }

        } while (true);
    }

    /**
     * send and receive messages with the given username in private chat
     *
     * @param response Response received from sending message
     * @param username username of user we want to private chat with
     */
    private void sendAndReceiveMessage(Response response, String username) {
        //print previous messages
        String privateChatMessages = (String) response.getData();
        System.out.println(privateChatMessages);

        PrivateChat temp = this.user.doesPrivateChatExist(username);
        if (temp == null)
            temp = new PrivateChat(this.user.getUsername(), username);
        this.user.setCurrentChat(temp);

        listenForMessage(temp);
        sendMessage(username, RequestType.SEND_MESSAGE);
    }

}