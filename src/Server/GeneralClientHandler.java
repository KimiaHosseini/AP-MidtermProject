package Server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;

import DiscordFeatures.*;
import Handler.ResponseStatus;
import Model.Request;
import Model.RequestType;
import Model.Response;
import UserFeatures.Status;
import UserFeatures.User;

/**
 * GeneralClientHandler handlers all the requests sent from the client.
 * Each client has a clientHandler run on a separate thread for them on the server side.
 * ClientHandlers must connect to the server with socket and so the
 * GeneralClientHandler extends ClientHandler to extend this feature
 */
public class GeneralClientHandler extends ClientHandler implements Runnable {

    private final Socket clientSocket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private User user;
    private final static File usersFile = new File("./Files/userFiles.txt");
    private static ArrayList<GeneralClientHandler> clientHandlers;
    private static ArrayList<User> users;

    /**
     * creates clientHandler for given user to be able to send data to all clients
     *
     * @param user given user
     */
    public GeneralClientHandler(User user) {
        super();
        this.clientSocket = null;
        this.user = user;
    }

    /**
     * creates client handler that is connected from the server
     * and saves the clientSocket which it connected from as the socket
     *
     * @param clientSocket socket which the client has connected from
     */
    public GeneralClientHandler(Socket clientSocket) {
        super(clientSocket);
        try {
            oos = new ObjectOutputStream(super.getSocket().getOutputStream());
            ois = new ObjectInputStream(super.getSocket().getInputStream());
        } catch (IOException e) {
            shutDown(ois, oos, clientSocket);
            System.out.println("shut down2");
        }
        this.clientSocket = clientSocket;
    }

    /**
     * run method for ClientHandler.
     * This method receives all requests and handles each one accordingly
     * based on the requestType and sends a Response back to client
     */
    @Override
    public void run() {
        while (true) {
            Request<?> request;
            do {
                try {
                    request = (Request<?>) ois.readUnshared();
                } catch (IOException | ClassNotFoundException e) {
                    shutDown(ois, oos, clientSocket);
                    System.out.println("shut down3");
                    return;
                }

                Response response = getResponse(request);

                if (request.getRequestType() == RequestType.SEND_MESSAGE) {
                    try {
                        String username = getChatReceiver((String) request.getData("username"));
                        if (username == null) break;
                        GeneralClientHandler clientHandler = findClientHandler(username);

                        if (clientHandler.oos != null && clientHandler.getUser().isInThisChat(this.user.getUsername())) {
                            clientHandler.oos.writeObject(response);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (request.getRequestType() == RequestType.SEND_MESSAGE_TO_CHANNEL) {
                    try {
                        boolean b = getChatReceiver((String) request.getData("serverIndex"), (String) request.getData("channelIndex"));
                        if (!b) {
                            System.out.println(request.getData("message"));
                            if (response.getResponseStatus().equals(ResponseStatus.BANNED_USER))
                                findClientHandler(user).oos.writeUnshared(new Response((ResponseStatus.BANNED_USER), new Message("You have been banned. Enter 0 to go back.")));
                            break;
                        }

                        if (response.getResponseStatus().equals(ResponseStatus.VIEW_ONLY)) {
                            findClientHandler(user).oos.writeUnshared(new Response((ResponseStatus.BANNED_USER), new Message("This channel is view only.")));
                            break;
                        }
                        ArrayList<GeneralClientHandler> clientHandlers = getAllChannelClientHandlers((String) request.getData("channelIndex"), (String) request.getData("serverIndex"));
                        for (GeneralClientHandler channelClientHandler : clientHandlers) {
                            if (channelClientHandler.oos != null && channelClientHandler.getUser().isInThisChat(user.getCurrentChat()) && !user.isBlockedAlready(channelClientHandler.user)) {
                                channelClientHandler.oos.writeUnshared(response);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        if (response != null) oos.writeUnshared(response);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }

            } while (true);
        }
    }

    /**
     * gets a Response for requests sent from client based on the different RequestType
     *
     * @param request given requestType of request sent from client
     * @return corresponding response to the request
     */
    @SuppressWarnings("unchecked")
    public Response getResponse(Request<?> request) {

        RequestType requestType = request.getRequestType();
        Response response = null;

        //میتونیم wildcard بالا رو string بنویسیم و همه این کست ها حذف شوند
        switch (requestType) {
            case CHECK_USERNAME -> response = checkUsernameResponse((Request<String>) request);
            case SIGN_UP -> response = signUpResponse((Request<String>) request);
            case SIGN_IN -> response = signInResponse((Request<String>) request);
            case SIGN_OUT -> response = signOutResponse();

            case PROFILE_PHOTO -> profilePhotoResponse((Request<String>) request);
            case SET_STATUS -> response = setStatusResponse((Request<String>) request);
            case CHANGE_PASSWORD -> response = changePassword((Request<String>) request);

            case PRINT_FRIEND_REQUESTS -> response = printFriendRequestResponse((Request<String>) request);
            case FRIEND_REQUEST -> response = friendRequestResponse((Request<String>) request);
            case ACCEPT_FRIEND_REQUEST -> response = acceptFriendRequest((Request<String>) request);
            case DELETE_FRIEND_REQUEST -> response = deleteFriendRequest((Request<String>) request);
            case PRINT_FRIENDS -> response = printFriendResponse((Request<String>) request);

            case BLOCK_FRIEND -> response = blockFriendResponse((Request<String>) request);
            case PRINT_BLOCKED_FRIENDS -> response = printBlockedUsersResponse((Request<String>) request);
            case UNBLOCK_USER -> response = unblockFriendResponse((Request<String>) request);

            case PRINT_PRIVATE_CHATS_USERNAMES -> response = printPrivateChatUsernamesResponse((Request<String>) request);
            case PRIVATE_CHAT_MESSAGES -> response = privateChatResponse((Request<String>) request);
            case CHECK_USERNAME_FOR_CHAT -> response = checkUsernameForChat((Request<String>) request);
            case ADD_FILE_TO_CHAT -> addFileToChat((Request<String>) request);
            case ADD_FILE_TO_CHANNEL -> addFileToChannel((Request<String>) request);
            case PRINT_FILE_NAMES -> response = printFileNames((Request<String>) request);
            case PRINT_FILE_NAMES_IN_CHANNEL -> response = printFileNamesInChannel((Request<String>) request);
            case DOWNLOAD_FILE -> response = downloadFile((Request<String>) request);
            case DOWNLOAD_FILE_IN_CHANNEL -> response = downLoadFileInChannel((Request<String>) request);
            case CLOSE_CHAT -> response = closeChat();
            case CLOSE_THREAD -> this.user.setChatToNull();
            case SET_CURRENT_CHAT -> setCurrentChat((Request<String>) request);

            case SEND_MESSAGE -> response = sendMessage((Request<String>) request);

            case SEND_MESSAGE_TO_CHANNEL -> response = sendChannelMessage((Request<String>) request);

            case PRINT_SERVERS -> response = printServers();
            case PRINT_CHANNELS -> response = printChannels((Request<String>) request);
            case NEW_SERVER -> response = createNewServer((Request<String>) request);
            case INVITE_TO_SERVER -> response = inviteUserToServer((Request<String>) request);
            case ENTER_CHANNEL -> response = enterChannel((Request<String>) request);
            case GET_CHANNEL -> response = getChannel((Request<String>) request);
            case GET_SERVER -> response = getServer((Request<String>) request);

            case NEW_CHANNEL -> response = createNewChannel((Request<String>) request);
            case DELETE_CHANNEL -> response = deleteChannel((Request<String>) request);
            case CHANGE_SERVER_NAME -> response = changeServerName((Request<String>) request);
            case DELETE_SERVER -> response = deleteServer((Request<String>) request);
            case BAN_MEMBER -> response = banMember((Request<String>) request);
            case GET_BANNED_MEMBERS -> response = getBannedMembers((Request<String>) request);
            case UNBAN_MEMBER -> response = unbanMember((Request<String>) request);
            case GET_SERVER_MEMBERS -> response = getServerMembers((Request<String>) request);
            case NEW_ROLE -> response = createNewRole((Request<Role>) request);
            case GET_ROLES -> response = getRoles((Request<String>) request);
            case ASSIGN_ROLE -> response = assignRole((Request<String>) request);
            case GET_MEMBER_ROLES -> response = getMemberRoles((Request<String>) request);
            case DELETE_ROLE -> response = deleteRole((Request<String>) request);
            case VIEW_PINNED -> response = viewPinnedMessages((Request<String>) request);
            case PIN_MESSAGE -> response = pinMessage((Request<String>) request);
            case LEAVE_SERVER -> response = leaveServer((Request<String>) request);
            case GET_NUMBERED_MESSAGES -> response = getChannelMessages((Request<String>) request);
            case VIEW_MEMBERS -> response = viewMembers((Request<String>) request);
            case VIEW_ONLY -> response = viewOnly((Request<String>) request);
            case REVOKE_ACCESS -> response = revokeUserAccess((Request<String>) request);

        }
        return response;
    }

    /**
     * gets all generalClientHandlers of the users in a given channel of a given server
     *
     * @param channelIndex given channel
     * @param serverIndex  given server
     * @return arrayList of all generalClientHandlers
     */
    private ArrayList<GeneralClientHandler> getAllChannelClientHandlers(String channelIndex, String serverIndex) {
        Channel channel = findChannel(channelIndex, serverIndex);

        ArrayList<GeneralClientHandler> channelClientHandlers = new ArrayList<>();
        ArrayList<User> channelUsers = channel.getUsers();
        for (User user : channelUsers) {
            if (user != this.user) {
                channelClientHandlers.add(findClientHandler(user));
            }
        }

        return channelClientHandlers;

    }

    /**
     * @param username username of user of requested private chat
     * @return private chat with that user
     */
    private String getChatReceiver(String username) {
        PrivateChat pv = (PrivateChat) findChat(username);
        User user = findUser(username);
        if (user.getCurrentChat() == null || !user.getCurrentChat().equals(pv)) return null;
        return username;
    }

    /**
     *
     * @param serverIndex given server
     * @param channelIndex given channel
     * @return channel from specified server and channel index
     */
    private boolean getChatReceiver(String serverIndex, String channelIndex) {
        Channel channel = findChannel(channelIndex, serverIndex);

        return user.getCurrentChat() != null && user.getCurrentChat().equals(channel);
    }

    /**
     * action invoked to make a channel view only for a user
     *
     * @param request request with server and channel index and username
     * @return Response status
     */
    private Response revokeUserAccess(Request<String> request) {
        int serverIndex = Integer.parseInt(request.getData("serverIndex"));
        int channelIndex = Integer.parseInt(request.getData("channelIndex"));
        Channel channel = user.getServer(serverIndex).getChannel(channelIndex);
        if (channel == null) return new Response(ResponseStatus.INVALID_STATUS);
        User user = findUser(request.getData("username"));
        if (user == null) return new Response(ResponseStatus.INVALID_STATUS);

        user.getServer(serverIndex).getChannel(channelIndex).removeUser(user);
        return new Response(ResponseStatus.VALID_STATUS);
    }

    /**
     * action invoked to check if a channel is view only for a user
     *
     * @param request request with server and channel index and username
     * @return Response status
     */
    private Response viewOnly(Request<String> request) {
        int serverIndex = Integer.parseInt(request.getData("serverIndex"));
        int channelIndex = Integer.parseInt(request.getData("channelIndex"));
        Channel channel = user.getServer(serverIndex).getChannel(channelIndex);
        if (channel == null) return new Response(ResponseStatus.INVALID_STATUS);
        User user = findUser(request.getData("username"));
        if (user == null) return new Response(ResponseStatus.INVALID_STATUS);

        user.getServer(serverIndex).getChannel(channelIndex).makeViewOnly(user);
        return new Response(ResponseStatus.VALID_STATUS);

    }

    /**
     * action invoked to view members of specific server
     *
     * @param request request with server index
     * @return Response status with String of members
     */
    private Response viewMembers(Request<String> request) {
        int serverIndex = Integer.parseInt(request.getData("serverIndex"));
        if (user.getServer(serverIndex) == null) {
            return new Response(ResponseStatus.VALID_STATUS, "This server has been deleted.");
        }
        return new Response(ResponseStatus.VALID_STATUS, user.getServer(serverIndex).getServerMembersString());
    }

    /**
     * action invoked to leave specific server
     *
     * @param request request with server index
     * @return Response status
     */
    private Response leaveServer(Request<String> request) {
        int serverIndex = Integer.parseInt(request.getData("serverIndex"));
        user.getServer(serverIndex).removeMember(request.getData("username"));
        findUser(request.getData("username")).removeServer(user.getServer(serverIndex));

        return new Response(ResponseStatus.VALID_STATUS);
    }

    /**
     * action invoked to pin message to specific channel of specific server
     *
     * @param request request with server and channel and message index
     * @return Response status
     */
    private Response pinMessage(Request<String> request) {
        int serverIndex = Integer.parseInt(request.getData("serverIndex"));
        int channelIndex = Integer.parseInt(request.getData("channelIndex"));
        int messageIndex = Integer.parseInt(request.getData("messageIndex"));
        if (user.getServer(serverIndex).getChannel(channelIndex).pinMessage(messageIndex))
            return new Response(ResponseStatus.VALID_STATUS);

        return new Response(ResponseStatus.INVALID_STATUS);
    }

    /**
     * action invoked to view pinned messages to specific channel of specific server
     *
     * @param request request with server and channel index
     * @return Response status with String of pinned messages
     */
    private Response viewPinnedMessages(Request<String> request) {
        return new Response(ResponseStatus.VALID_STATUS, user.getServer(Integer.parseInt(request.getData("serverIndex"))).getChannel(Integer.parseInt(request.getData("channelIndex"))).getPinnedMessages());
    }

    /**
     * action invoked to get roles to a specific member
     *
     * @param request request with server index and username
     * @return Response status and String of roles
     */
    private Response getMemberRoles(Request<String> request) {
        int serverIndex = Integer.parseInt(request.getData("serverIndex"));
        HashSet<Role> roles = user.getServer(serverIndex).getMemberRoles(request.getData("username"));
        if (roles == null) {
            return new Response(ResponseStatus.INVALID_USERNAME);
        }

        String s = "";
        if (roles.isEmpty()) s = "Empty\n";

        for (Role role : roles) {
            s = s.concat(role.getName());
        }

        return new Response(ResponseStatus.VALID_STATUS, s);
    }

    /**
     * action invoked to delete role to a specific member
     *
     * @param request request with server and role index and username
     * @return Response status
     */
    private Response deleteRole(Request<String> request) {
        int serverIndex = Integer.parseInt(request.getData("serverIndex"));
        String roleName = request.getData("role");
        if (user.getServer(serverIndex).deleteMemberRole(request.getData("username"), roleName))
            return new Response(ResponseStatus.VALID_STATUS);

        return new Response(ResponseStatus.INVALID_STATUS);
    }

    /**
     * action invoked to assign role to a specific member
     *
     * @param request request with server and role index and username
     * @return Response status
     */
    private Response assignRole(Request<String> request) {
        int serverIndex = Integer.parseInt(request.getData("serverIndex"));
        int roleIndex = Integer.parseInt(request.getData("roleIndex"));
        Role role = user.getServer(serverIndex).getRole(roleIndex);
        if (user.getServer(serverIndex).assignRole(request.getData("username"), role)) {
            return new Response(ResponseStatus.VALID_STATUS);
        }
        return new Response(ResponseStatus.INVALID_STATUS);
    }

    /**
     * action invoked to get roles of specific server
     *
     * @param request request with server index
     * @return Response status with String of roles
     */
    private Response getRoles(Request<String> request) {
        int serverIndex = Integer.parseInt(request.getData("serverIndex"));
        String roles = user.getServer(serverIndex).getServerRolesString();
        return new Response(ResponseStatus.VALID_STATUS, roles);
    }

    /**
     * action invoked to get create and add a new role to a server
     *
     * @param request request with server index and role
     * @return Response status
     */
    private Response createNewRole(Request<Role> request) {
        Role role = request.getData("role");
        int serverIndex = Integer.parseInt(request.getData("roleServer").getName());
        user.getServer(serverIndex).addRole(role);
        user.getServer(serverIndex).getServerRolesString();
        return new Response(ResponseStatus.VALID_STATUS);
    }

    /**
     * action invoked to get members of specific server
     *
     * @param request request with server index
     * @return Response status with String of members
     */
    private Response getServerMembers(Request<String> request) {
        int serverIndex = Integer.parseInt(request.getData("serverIndex"));
        String members = user.getServer(serverIndex).getServerMembersString();
        return new Response(ResponseStatus.VALID_STATUS, members);
    }

    /**
     * action invoked to get banned members of specific server
     *
     * @param request request with server index
     * @return Response status with String of banned members
     */
    private Response getBannedMembers(Request<String> request) {
        String bannedMembers = user.getServer(Integer.parseInt(request.getData("serverIndex"))).getBannedMembersString();
        return new Response(ResponseStatus.VALID_STATUS, bannedMembers);
    }

    /**
     * action invoked to unban a user from a specific server
     *
     * @param request request with server index and username
     * @return Response status
     */
    private Response unbanMember(Request<String> request) {
        if (user.getServer(Integer.parseInt(request.getData("serverIndex"))).removeBannedUser(Integer.parseInt(request.getData("userIndex"))))
            return new Response(ResponseStatus.VALID_STATUS);
        return new Response(ResponseStatus.INVALID_STATUS);
    }

    /**
     * action invoked to ban a user from a specific server
     *
     * @param request request with server index and username
     * @return Response status
     */
    private Response banMember(Request<String> request) {
        int serverIndex = Integer.parseInt(request.getData("serverIndex"));
        if (user.getServer(serverIndex).banMember(request.getData("username"))) {
            findUser(request.getData("username")).removeServer(user.getServer(serverIndex));
            return new Response(ResponseStatus.VALID_STATUS);
        }
        return new Response(ResponseStatus.INVALID_STATUS);
    }

    /**
     * action invoked to get specified server
     *
     * @param request request with server
     * @return Response status and requested server
     */
    private Response getServer(Request<String> request) {
        if (user.getServer(Integer.parseInt((request.getData("serverIndex")))) == null)
            return new Response(ResponseStatus.INVALID_STATUS);
        return new Response(ResponseStatus.VALID_STATUS, user.getServer(Integer.parseInt((request.getData("serverIndex")))));
    }

    /**
     * action invoked to delete specified server
     *
     * @param request request with server index
     * @return Response status
     */
    private Response deleteServer(Request<String> request) {
        int serverIndex = Integer.parseInt(request.getData("serverIndex"));
        DiscordServer server = user.getServer(serverIndex);
        ArrayList<User> members = user.getServer(serverIndex).getMembers();
        for (User user : members) {
            user.removeServer(server);
        }

        return new Response(ResponseStatus.VALID_STATUS);
    }

    /**
     * action invoked to change name of specified server
     *
     * @param request request with server name and server index
     * @return Response status
     */
    private Response changeServerName(Request<String> request) {
        int serverIndex = Integer.parseInt(request.getData("serverIndex"));
        user.getServer(serverIndex).setName(request.getData("newName"));
        return new Response(ResponseStatus.VALID_STATUS);
    }

    /**
     * action invoked to delete specified channel in specified server
     *
     * @param request request with server and channel index
     * @return Response status
     */
    private Response deleteChannel(Request<String> request) {
        int serverIndex = Integer.parseInt(request.getData("serverIndex"));
        int channelIndex = Integer.parseInt(request.getData("channelIndex"));
        if (user.getServer(serverIndex).getChannel(channelIndex) == null) {
            return new Response(ResponseStatus.INVALID_STATUS);
        }
        user.getServer(serverIndex).deleteChannel(channelIndex);
        return new Response(ResponseStatus.VALID_STATUS);
    }

    /**
     * action invoked to get specified channel in specified server
     *
     * @param request request with server and channel index
     * @return Response status and requested channel
     */
    private Response getChannel(Request<String> request) {
        int serverIndex = Integer.parseInt(request.getData("serverIndex"));
        int channelIndex = Integer.parseInt(request.getData("channelIndex"));
        Channel channel = user.getServer(serverIndex).getChannel(channelIndex);
        if (channel == null) return new Response(ResponseStatus.INVALID_STATUS);
        user.setCurrentChat(channel);
        return new Response(ResponseStatus.VALID_STATUS, channel);
    }

    /**
     * action invoked to create new channel in specified server
     *
     * @param request request with server index and channel name
     * @return Response status and new channel
     */
    private Response createNewChannel(Request<String> request) {
        int serverIndex = Integer.parseInt(request.getData("serverIndex"));
        user.getServer(serverIndex).addChannel(request.getData("name"));
        return new Response(ResponseStatus.VALID_STATUS, user.getServer(serverIndex).getRecentlyAddedChannel());
    }

    /**
     * action invoked to invite a specified user to specified server
     *
     * @param request request with server index and username
     * @return Response status and updated user with specified username
     */
    private Response inviteUserToServer(Request<String> request) {
        int serverIndex = Integer.parseInt(request.getData("serverIndex"));
        User invitedFriend = findUser(request.getData("username"));

        if (!user.isInFriends(invitedFriend)) {
            return new Response(ResponseStatus.INVALID_USERNAME);
        } else if (invitedFriend.isInServer(user.getServer(serverIndex))) {
            return new Response(ResponseStatus.INVALID_STATUS);
        } else if (user.getServer(serverIndex).isBanned(request.getData("username")))
            return new Response(ResponseStatus.BANNED_USER);

        invitedFriend.addServer(user.getServer(serverIndex));
        user.getServer(serverIndex).addMember(invitedFriend);
        return new Response(ResponseStatus.VALID_STATUS, invitedFriend);
    }

    /**
     * action invoked to get String of users servers
     *
     * @return Response status and String of servers
     */
    private Response printServers() {
        return new Response(ResponseStatus.VALID_STATUS, user.serversToString());
    }

    /**
     * action invoked to get String of users channels in specific server
     *
     * @param request request with server index
     * @return Response status and String of channels
     */
    private Response printChannels(Request<String> request) {
        int serverIndex = Integer.parseInt(request.getData("requestedServer"));
        if (user.getServer(serverIndex) == null) {
            return new Response(ResponseStatus.VALID_STATUS, "This server has been deleted.");
        }
        return new Response(ResponseStatus.VALID_STATUS, user.getServer(serverIndex).getChannelsToString(user));
    }

    /**
     * action invoked to create new server
     *
     * @param request request with server name
     * @return Response status and new server
     */
    private Response createNewServer(Request<String> request) {
        String serverName = request.getData("name");
        DiscordServer discordServer = new DiscordServer(serverName, user);
        user.addServer(discordServer);
        return new Response(ResponseStatus.VALID_STATUS, discordServer);
    }

    /**
     * action invoked to get specific channel of specific server
     *
     * @param request request with server and channel index
     * @return Response with messages in the channel
     */
    private Response getChannelMessages(Request<String> request) {
        int serverIndex = Integer.parseInt(request.getData("serverIndex"));
        int channelIndex = Integer.parseInt(request.getData("channelIndex"));
        if (user.getServer(serverIndex).getChannel(channelIndex) == null) {
            return new Response(ResponseStatus.INVALID_STATUS);
        }
        String channelMessages = "You do not have permission to view chat history.";
        if (!user.getServer(serverIndex).haveThisAccessibility(user, Permissions.CANT_VIEW_CHAT_HISTORY)) {
            channelMessages = user.getServer(serverIndex).getChannel(channelIndex).getMessagesAsString();
        }
        return new Response(ResponseStatus.VALID_STATUS, channelMessages);
    }

    /**
     * action invoked to get specific channel of specific server.
     * Also sets users current chat to channel
     *
     * @param request request with server and channel index
     * @return Response with messages in the channel
     */
    private Response enterChannel(Request<String> request) {
        int serverIndex = Integer.parseInt(request.getData("serverIndex"));
        int channelIndex = Integer.parseInt(request.getData("channelIndex"));
        if (user.getServer(serverIndex) == null) {
            return new Response(ResponseStatus.INVALID_STATUS);
        }
        if (user.getServer(serverIndex).getChannel(channelIndex) == null) {
            return new Response(ResponseStatus.INVALID_STATUS);
        }
        String channelMessages = "You do not have permission to view chat history.";
        if (!user.getServer(serverIndex).haveThisAccessibility(user, Permissions.CANT_VIEW_CHAT_HISTORY)) {
            channelMessages = user.getServer(serverIndex).getChannel(channelIndex).getMessagesNotNumbered();
        }
        user.setCurrentChat(user.getServer(serverIndex).getChannel(channelIndex));
        return new Response(ResponseStatus.VALID_STATUS, channelMessages);
    }

    /**
     * invokes action to check username. returns valid response if the username exists in users
     *
     * @param request request with username
     * @return Response status
     */
    private Response checkUsernameResponse(Request<String> request) {
        if (findUser(request.getData("username")) != null) return new Response(ResponseStatus.INVALID_USERNAME);

        return new Response(ResponseStatus.VALID_USERNAME);
    }

    /**
     * invokes action to sign up user
     *
     * @param request request with username, password, email, and phoneNum of the user that wants to sign up
     * @return Response status
     */
    private Response signUpResponse(Request<String> request) {
        this.user = new User(request.getData("username"), request.getData("password"), request.getData("email"), request.getData("phoneNum"));
        addClientHandler(this);
        return new Response(ResponseStatus.SIGNUP_VALID);
    }

    /**
     * invokes action to sign in user. Also sets user status to online
     *
     * @param request request with username of the user that wants to sign in.
     * @return Response status
     */
    private Response signInResponse(Request<String> request) {
        User user = findUser(request.getData("username"));
        Response response;
        if (user == null || !user.checkPassword(request.getData("pass")))
            response = new Response(ResponseStatus.SIGN_IN_INVALID);
        else {
            response = new Response(ResponseStatus.SIGN_IN_VALID, user);
            if (user.getStatus() == Status.OFFLINE) user.setStatus(Status.ONLINE);
            this.user = user;
            clientHandlers.remove(findClientHandler(this.user.getUsername()));
            clientHandlers.add(this);
        }

        return response;
    }

    /**
     * invokes action to sign out of user. Also sets user status to offline
     *
     * @return Response status
     */
    private Response signOutResponse() {
        if (user != null) {
            user.setStatus(Status.OFFLINE);
            user.setChatToNull();
        }
        writeUsersToFile();
        return new Response(ResponseStatus.SIGN_OUT_VALID);
    }

    /**
     * invokes action to set pfp of user
     *
     * @param request request with username of the user that wants to set pfp
     */
    private void profilePhotoResponse(Request<String> request) {
        String profilePhoto = request.getData("profilePhoto");
        this.user.setPfp(new File(profilePhoto));
    }

    /**
     * invokes action to change password of user
     *
     * @param request request with username of the user that wants to change password
     * @return Response status
     */
    private Response changePassword(Request<String> request) {
        String username = request.getData("username");
        String newPass = request.getData("pass");
        User user = findUser(username);
        if (user.checkPassword(newPass)) return new Response(ResponseStatus.SAME_PASSWORD);
        user.setPassword(newPass);
        return new Response(ResponseStatus.VALID_CHANGE_PASSWORD);
    }

    /**
     * invokes action to set status of user
     *
     * @param request request with username of the user that wants to set status
     * @return Response status
     */
    private Response setStatusResponse(Request<String> request) {
        int statusIndex = Integer.parseInt(request.getData("status"));
        if (statusIndex >= 0 && statusIndex < 5) {
            this.user.setStatus(statusIndex);
            return new Response(ResponseStatus.VALID_STATUS);
        }
        return new Response(ResponseStatus.INVALID_STATUS);
    }

    /**
     * invokes action to get list of friend requests
     *
     * @param request request with username of the user that wants list of friend requests
     * @return Response status and String of list of friend requests
     */
    private Response printFriendRequestResponse(Request<String> request) {
        User user = findUser(request.getData("username"));
        String data = user.getRequestsListAsString();
        if (data.equals("Empty\n")) return new Response(ResponseStatus.EMPTY_FRIEND_REQUEST_LIST, data);
        return new Response(ResponseStatus.NOT_EMPTY_FRIEND_REQUEST_LIST, data);
    }

    /**
     * invokes action to send friend request to specified user
     *
     * @param request request with username of the user that is being sending request and the user that is recieving request
     * @return Response status
     */
    private Response friendRequestResponse(Request<String> request) {
        String receiverUsername = request.getData("receiver");
        String requesterUsername = request.getData("requester");
        User receiver = findUser(receiverUsername);
        User requester = findUser(requesterUsername);
        Response response;
        if (receiver == null) response = new Response(ResponseStatus.INVALID_USERNAME);
        else if (receiver.isRequestedAlready(requester))
            response = new Response(ResponseStatus.DUPLICATE_FRIEND_REQUEST);
        else if (receiver.isInFriends(requester)) response = new Response(ResponseStatus.FRIEND_REQUEST_TO_FRIEND);
        else {
            response = new Response(ResponseStatus.VALID_FRIEND_REQUEST);
            receiver.addRequest(requester);
        }
        return response;
    }

    /**
     * invokes action to accept friend request of specified user
     *
     * @param request request with username of the user that is accepting request and the user that sent request
     * @return Response status
     */
    private Response acceptFriendRequest(Request<String> request) {
        String receiverUsername = request.getData("receiver");
        String requesterUsername = request.getData("requester");
        User receiver = findUser(receiverUsername);
        User requester = receiver.findRequest(requesterUsername);
        if (requester == null) return new Response(ResponseStatus.INVALID_ACCEPT_FRIEND_REQUEST);
        receiver.acceptRequest(requester);
        return new Response(ResponseStatus.VALID_ACCEPT_FRIEND_REQUEST);
    }

    /**
     * invokes action to delete friend request of specified user
     *
     * @param request request with username of the user that is deleting request and the user that sent request
     * @return Response status
     */
    private Response deleteFriendRequest(Request<String> request) {
        String receiverUsername = request.getData("receiver");
        String requesterUsername = request.getData("requester");
        User receiver = findUser(receiverUsername);
        User requester = receiver.findRequest(requesterUsername);
        if (requester == null) return new Response(ResponseStatus.INVALID_DELETE_FRIEND_REQUEST);
        receiver.removeRequest(requester);
        return new Response(ResponseStatus.VALID_DELETE_FRIEND_REQUEST);
    }

    /**
     * invokes action to get list of friends
     *
     * @param request request with username of the user that wants list of friends
     * @return Response status and String of list of friends
     */
    private Response printFriendResponse(Request<String> request) {
        String username = request.getData("username");
        User user = findUser(username);
        return new Response(ResponseStatus.VALID_STATUS, user.getFriendsListAsString());
    }

    /**
     * invokes action to get list of blocked users
     *
     * @param request request with username of the user that wants list of blocked users
     * @return Response status and String of list of blocked users
     */
    private Response printBlockedUsersResponse(Request<String> request) {
        String username = request.getData("username");
        User user = findUser(username);
        String data = user.getBlockedListAsString();
        if (data.equals("Empty\n")) return new Response(ResponseStatus.EMPTY_BLOCKED_USERS_LIST, data);
        return new Response(ResponseStatus.NOT_EMPTY_BLOCKED_USERS_LIST, data);
    }

    /**
     * invokes action to block specified user
     *
     * @param request request with username of the user that is being blocked and the user that is blocking
     * @return Response status
     */
    private Response blockFriendResponse(Request<String> request) {
        String blockedUsername = request.getData("blocked");
        String requesterUsername = request.getData("requester");
        User blocked = findUser(blockedUsername);
        User requester = findUser(requesterUsername);
        Response response;
        if (blocked == null || !requester.isInFriends(blocked)) response = new Response(ResponseStatus.INVALID_BLOCK);
        else if (requester.isBlockedAlready(blocked)) response = new Response(ResponseStatus.DUPLICATE_BLOCK);
        else {
            response = new Response(ResponseStatus.VALID_BLOCK);
            requester.addBlockedUser(blocked);
        }
        return response;
    }

    /**
     * invokes action to unblock specified user
     *
     * @param request request with username of the user that is blocked and the user that is unblocking
     * @return Response status
     */
    private Response unblockFriendResponse(Request<String> request) {
        String unblockUsername = request.getData("unblocked");
        String requesterUsername = request.getData("requester");
        User unblock = findUser(unblockUsername);
        User requester = findUser(requesterUsername);
        Response response;
        if (unblock == null || !requester.isBlockedAlready(unblock))
            response = new Response(ResponseStatus.INVALID_UNBLOCK);
        else {
            response = new Response(ResponseStatus.VALID_UNBLOCK);
            requester.unblock(unblock);
        }
        return response;
    }

    /**
     * invokes action to get all usernames of users the client has private chats with
     *
     * @param request request with username of client
     * @return Response status with string of all usernames of private chats
     */
    private Response printPrivateChatUsernamesResponse(Request<String> request) {
        String username = request.getData("username");
        User user = findUser(username);
        return new Response(ResponseStatus.VALID_STATUS, user.getPrivateChatsUsernamesListAsString());
    }

    /**
     * invokes action to see respond to request if the client has a
     * private chat with the user with the specified username or not
     *
     * @param request request with username of user we want private chat with
     * @return Response status
     */
    private Response checkUsernameForChat(Request<String> request) {
        String person2username = request.getData("username");
        User person1 = this.user;
        User person2 = findUser(person2username);

        if (person1 == null || person2 == null) return new Response(ResponseStatus.INVALID_USERNAME);

        if (person1.isBlockedAlready(person2) || person2.isBlockedAlready(person1))
            return new Response(ResponseStatus.BLOCKED_USERNAME);

        return new Response(ResponseStatus.VALID_STATUS);
    }

    /**
     * action invoked to get private Chats with a specific user
     *
     * @param privateChatRequest request with username of user we want private chat with
     * @return Response with messages in the private chat
     */
    private Response privateChatResponse(Request<String> privateChatRequest) {
        String person2username = privateChatRequest.getData("username");
        PrivateChat privateChat = (PrivateChat) findChat(person2username);

        this.user.setChatToNull();
        return new Response(ResponseStatus.VALID_STATUS, privateChat.getMessagesNotNumbered());
    }

    /**
     * finds private chat with username of the username of the user that is being chatted with
     *
     * @param username username of other user
     * @return Private chat with other user
     */
    private Chat findChat(String username) {
        User person1 = this.user;
        User person2 = findUser(username);

        PrivateChat privateChat = person1.doesPrivateChatExist(username);

        if (privateChat == null) {
            privateChat = new PrivateChat(person1.getUsername(), username);
            person1.addPrivateChat(privateChat);
            person2.addPrivateChat(privateChat);
        }
        return privateChat;
    }

    /**
     * sets current chat to the requested private chat
     *
     * @param request request with username that provides private chat information
     */
    private void setCurrentChat(Request<String> request) {
        String person2username = request.getData("username");
        PrivateChat privateChat = (PrivateChat) findChat(person2username);
        this.user.setCurrentChat(privateChat);
    }

    /**
     * action invoked to send a message to a private chat
     *
     * @param request request with message content and private chat information
     * @return Response status with message
     */
    private Response sendMessage(Request<String> request) {
        String text = request.getData("message");
        String username = request.getData("username");
        Message message = new Message(text);

        if (text.substring(text.indexOf(":") + 2).equals("0")) return null;

        findChat(username).addMessage(message);

        return new Response(ResponseStatus.VALID_MESSAGE, message);
    }

    /**
     * action invoked to send a message to a channel
     *
     * @param request request with message content and channel information
     * @return Response status with message
     */
    private Response sendChannelMessage(Request<String> request) {
        String text = request.getData("message");
        int serverIndex = Integer.parseInt(request.getData("serverIndex"));
        int channelIndex = Integer.parseInt(request.getData("channelIndex"));
        Channel channel = user.getServer(serverIndex).getChannel(channelIndex);
        if (channel == null) return new Response(ResponseStatus.BANNED_USER);
        if (channel.isViewOnly(user)) return new Response(ResponseStatus.VIEW_ONLY);
        Message message = new Message(text);
        channel.addMessage(message);
        return new Response(ResponseStatus.VALID_MESSAGE, message);
    }

    /**
     * finds channel with given serverIndex and given channelIndex
     *
     * @param channelIndexStr given channel index
     * @param serverIndexStr  given server index
     * @return Channel
     */
    private Channel findChannel(String channelIndexStr, String serverIndexStr) {
        int serverIndex = Integer.parseInt(serverIndexStr);
        int channelIndex = Integer.parseInt(channelIndexStr);
        if (user.getServer(serverIndex) == null) {
            return null;
        }
        return user.getServer(serverIndex).getChannel(channelIndex);
    }

    /**
     * action invoked to upload a file to a private chat
     *
     * @param request request with file
     * @return Response status
     */
    private void addFileToChat(Request<String> request) {
        String username = request.getData("username");
        findChat(username).addFile(new File(request.getData("file")));
    }

    /**
     * action invoked to upload a file to a channel
     *
     * @param request request with file
     * @return Response status
     */
    private void addFileToChannel(Request<String> request) {
        findChannel(request.getData("channelIndex"), request.getData("serverIndex")).addFile(new File(request.getData("file")));
    }

    /**
     * action invoked to get file names from a private chat
     *
     * @param request request with data to access private chat
     * @return Response status with file names
     */
    private Response printFileNames(Request<String> request) {
        return new Response(null, findChat(request.getData("username")).getFilesNames());
    }

    /**
     * action invoked to get file names from a channel
     *
     * @param request request with data to access channel
     * @return Response status with file names
     */
    private Response printFileNamesInChannel(Request<String> request) {
        return new Response(null, findChannel(request.getData("channelIndex"), request.getData("serverIndex")).getFilesNames());
    }

    /**
     * action invoked to download a file from a private chat
     *
     * @param request request with data to access file
     * @return Response status with file data
     */
    private Response downloadFile(Request<String> request) {
        Chat chat = findChat(request.getData("username"));
        String fileName = request.getData("fileName");
        File file = chat.findFile(fileName);
        if (file != null) return new Response(ResponseStatus.FILE_DOWNLOADED, file);
        return new Response(ResponseStatus.INVALID_FILE_NAME);
    }

    /**
     * action invoked to download a file from a channel
     *
     * @param request request with data to access file
     * @return Response status with file data
     */
    private Response downLoadFileInChannel(Request<String> request) {
        Channel channel = findChannel(request.getData("channelIndex"), request.getData("serverIndex"));
        String fileName = request.getData("fileName");
        File file = channel.findFile(fileName);
        if (file != null) return new Response(ResponseStatus.FILE_DOWNLOADED, file);
        return new Response(ResponseStatus.INVALID_FILE_NAME);
    }

    /**
     * action invoked to do request of closing chat
     *
     * @return Response status
     */
    private Response closeChat() {
        user.setChatToNull();
        return new Response(ResponseStatus.CLOSE_THREAD);
    }

    /**
     * @return user of this clientHandler
     */
    private User getUser() {
        return user;
    }

    /**
     * sets the users in the clientHandler to the arrayList of users in the parameter
     *
     * @param users ArrayList of users
     */
    protected static void setUsers(ArrayList<User> users) {
        GeneralClientHandler.users = users;
    }

    /**
     * reads saved users from file into an ArrayList of users
     *
     * @return ArrayList of users that were read from file
     */
    @SuppressWarnings("unchecked")
    protected static ArrayList<User> readUsersFile() {
        ArrayList<User> c = new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(usersFile))) {
            c = (ArrayList<User>) ois.readObject();
        } catch (EOFException ignored) {
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        clientHandlers = new ArrayList<>();
        for (User user : c) {
            clientHandlers.add(new GeneralClientHandler(user));
        }

        return c;
    }

    /**
     * adds given ClientHandler to list of ClientHandlers and the ClientHandlers user
     * to the list of users and saves users to the file
     *
     * @param ch give ClientHandler
     */
    private void addClientHandler(GeneralClientHandler ch) {
        clientHandlers.add(ch);
        users.add(ch.getUser());
        writeUsersToFile();
    }

    /**
     * saves all users to file
     */
    private void writeUsersToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(usersFile))) {
            oos.writeObject(users);
        } catch (IOException e) {
            shutDown(ois, oos, clientSocket);
            System.out.println("shut down1");
        }
    }

    /**
     * @param username given username
     * @return returns user with the given username
     */
    public User findUser(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) return user;
        }
        return null;
    }

    /**
     * find the clientHandler of the user with the given username
     *
     * @param username given username
     * @return returns clientHandler of the user with the given username
     */
    public GeneralClientHandler findClientHandler(String username) {
        for (GeneralClientHandler clientHandler : clientHandlers) {
            if (clientHandler.getUser().getUsername().equals(username)) return clientHandler;
        }
        return null;
    }

    /**
     * find the clientHandler of the given user
     *
     * @param user given user
     * @return returns clientHandler of the given user
     */
    public GeneralClientHandler findClientHandler(User user) {
        for (GeneralClientHandler clientHandler : clientHandlers) {
            if (clientHandler.getUser().equals(user)) return clientHandler;
        }
        return null;
    }

    /**
     * shuts down all connections of the server to client
     *
     * @param in     ObjectInputStream from client
     * @param out    ObjectOutputStream to client
     * @param socket socket connection
     */
    private void shutDown(ObjectInputStream in, ObjectOutputStream out, Socket socket) {
        try {
            System.out.println("User disconnected.");
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            signOutResponse();
        }
    }

}