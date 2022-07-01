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

public class Client {
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private User user;

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

    private boolean ServerSettings() {
        return true;
    }


    public static void main(String[] args) {
        Socket socket;
        try {
            socket = new Socket("localhost", 7777);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Client client = new Client(socket);
        client.runClient();
    }

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

    synchronized private Response getResponse() {
        Response response = null;
        try {
            response = (Response) ois.readUnshared();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return response;
    }

    private void sendRequest(Request<?> request) {
        try {
            oos.writeUnshared(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void runClient() {

        System.out.println("\t".repeat(5) + "Welcome to Discord!");

        while (!socket.isClosed()) {

            System.out.println(MenuHandler.welcomeMenu());

            String menuAction = InputHandler.getString("Enter choice: ");
            switch (menuAction) {

                case "1" ->
                        signUp();

                case "2" -> {
                    if (!logIn()) {
                        break;
                    }
                    boolean continueLoop;
                    do {
                        continueLoop = userLoop();
                    } while (continueLoop);
                }

                case "0" ->
                        System.exit(0);

            }
        }
    }

    private void viewServers() {

        Request<String> printServers = new Request<>(RequestType.PRINT_SERVERS);
        sendRequest(printServers);
        Response response = getResponse();

        System.out.print((String) response.getData());

        String requestedServer = InputHandler.getString("Enter the numbered index of the server you want to enter: ");

        if (requestedServer.equals("0")) {
            return;
        }

        enterServer(requestedServer);

    }

    private void enterServer(String requestedServer) {
        do {
            MenuHandler.serverMenu();
            int menuChoice = InputHandler.getInt("Enter choice: ", 3);
            switch (menuChoice) {
                case 1 -> {
                    viewChannels(requestedServer);
                    enterChannel(requestedServer);
                }
                case 2 -> inviteFriendToServer(requestedServer);
                case 3 -> serverSettings(requestedServer);
                //case 4 -> viewPinnedMessages(requestedServer);
                case 0 -> {
                    return;
                }
            }
        } while (true);
    }

    private void sendAndReceiveMessage(Response response, String username){
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

    private void sendFile(String username){
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

    private void sendFile(String channelIndex, String serverIndex){
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

    private void downloadFile(String username){
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
        if (response.getResponseStatus() == ResponseStatus.INVALID_FILE_NAME){
            System.out.println("Invalid file name");
            return;
        }
        File file = (File) response.getData();
        downloadFile(file);
    }

    private void downloadFile(String channelIndex, String serverIndex){
        Request<String > fileNamesRequest = new Request<>(RequestType.PRINT_FILE_NAMES_IN_CHANNEL);
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
        if (response.getResponseStatus() == ResponseStatus.INVALID_FILE_NAME){
            System.out.println("Invalid file name");
            return;
        }
        File file = (File) response.getData();
        downloadFile(file);
    }
    private void serverSettings(String requestedServer) {
        MenuHandler.serverSettings(user.getServer(Integer.parseInt(requestedServer)), user);
        int menuChoice = InputHandler.getInt("Enter choice :", 10);
        switch (menuChoice) {
            case 1 -> changeServerName(requestedServer);
            //case 2 -> pinMessage();
            case 3 -> createChannel(requestedServer);
            case 4 -> deleteChannel(requestedServer);
            //case 5 -> modifyChannelAccess()
            //case 6 -> banMember(requestedServer);
            //case 7 -> createNewRole(requestedServer);
            //case 8 -> assignRoleToMember(requestedServer);
            case 9 -> deleteServer(requestedServer);
            case 0 -> {
                return;
            }
        }
    }

    private void createNewRole(String requestedServer) {
        //Role.createRole();
    }

    private void deleteServer(String requestedServer) {
        Request<String> deleteServer = new Request<>(RequestType.DELETE_SERVER);
        deleteServer.addData("serverIndex", requestedServer);
        if (InputHandler.getInt("Are you sure you want to delete the server?\n[1] No\n[2] Yes, delete", 3) == 2) {
            sendRequest(deleteServer);
            getResponse();
            System.out.println("Successfully deleted server.");
        }
    }

    private void changeServerName(String requestedServer) {
        Request<String> changeName = new Request<>(RequestType.CHANGE_SERVER_NAME);
        changeName.addData("serverIndex", requestedServer);
        String newName = InputHandler.getString("Enter new name for server: ");
        changeName.addData("newName", newName);
        sendRequest(changeName);
        getResponse();
        System.out.println("Successfully changed name.");
    }

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

    private void createChannel(String requestedServer) {
        Request<String> newChannel = new Request<>(RequestType.NEW_CHANNEL);
        newChannel.addData("serverIndex", requestedServer);
        String channelName = InputHandler.getString("Enter name of new channel : ");
        newChannel.addData("name", channelName);
        sendRequest(newChannel);
        getResponse();
        System.out.println("Successfully added channel.");
    }

    private void viewChannels(String requestedServer) {
        Request<String> printChannels = new Request<>(RequestType.PRINT_CHANNELS);
        printChannels.addData("requestedServer", requestedServer);
        sendRequest(printChannels);
        Response response = getResponse();
        System.out.print((String) response.getData());
    }

    private void enterChannel(String requestedServer) {
        String chosenChannel = InputHandler.getString("Enter index of channel you want to enter: ");
        if (chosenChannel.equals("0"))
            return;
        Channel channel;
        while (true){
            int choice = InputHandler.getInt("\n[1] Send Message\n[2] Send File\n[3] Download File", 3);

//            switch (choice){
//                case 1 -> {
//
//                }
//            }
            if (choice == 1){
                if (printChannelMessages(requestedServer, chosenChannel)) {
                    Request<String> getChannel = new Request<>(RequestType.GET_CHANNEL);
                    getChannel.addData("channelIndex", chosenChannel);
                    getChannel.addData("serverIndex", requestedServer);
                    sendRequest(getChannel);
                    channel = (Channel) getResponse().getData();
                    this.user.setCurrentChat(channel);
                }
                else
                    return;
                listenForMessage(channel);
                sendMessage(chosenChannel, requestedServer, RequestType.SEND_MESSAGE_TO_CHANNEL);
            } else if (choice == 2){
                closeChat();

                sendFile(chosenChannel, requestedServer);
                sendChannelMessage(chosenChannel, requestedServer, this.user.getUsername() + " sends a file.");
            } else if (choice == 3){
                closeChat();

                downloadFile(chosenChannel, requestedServer);
            } else if (choice == 0)
                return;
        }
    }

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
            System.out.println("Invalid index");
            return false;
        }
    }


    private void inviteFriendToServer(String requestedServer) {
        printList(RequestType.PRINT_FRIENDS);
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
        }
        User invitedFriend = (User) response.getData();
        //Request<DiscordServer> serverRequest =
        invitedFriend.addServer(user.getServer(Integer.parseInt(requestedServer)));
        System.out.println("Friend successfully added.");
    }

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
                            //close chat because we don't want to listen for messages any more
                            closeChat();

                            sendFile(username);
                            sendPrivateChatMessage(username, this.user.getUsername() + " sends a file.");
                        } else if (choice == 3) {
                            closeChat();

                            downloadFile(username);
                        }
                        else if (choice == 0)
                            break;
                    }
                }
            }
            case 3 -> printList(RequestType.PRINT_FRIENDS);
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

    private void closeChat(){
        Request<String> endRequest = new Request<>(RequestType.CLOSE_THREAD);
        sendRequest(endRequest);
        this.user.setChatToNull();
    }
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

    private void setPFP(String imagePath) {
        File profilePhoto = new File(imagePath);
        if (!profilePhoto.exists()){
            System.out.println("Invalid path");
            return;
        }
        Request<String> request = new Request<>(RequestType.PROFILE_PHOTO);
        request.addData("profilePhoto", imagePath);
        sendRequest(request);
    }

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
                System.out.println("Successfully signed in");
                break;
            }
        } while (true);

        return true;
    }


    private void sendFriendRequestProcess() {
        do {
            String receiver = InputHandler.getString("username: ");
            if (receiver.equals("0") || sendFriendRequest(receiver))
                return;
        } while (true);
    }

    private void blockFriendProcess() {
        printList(RequestType.PRINT_FRIENDS);
        do {
            String blockedUsername = InputHandler.getString("username: ");
            if (blockedUsername.equals("0") || blockFriend(blockedUsername))
                return;
        } while (true);
    }

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

    private Response printBlockedUsers() {
        Request<String> printBlocked = new Request<>(RequestType.PRINT_BLOCKED_FRIENDS);
        printBlocked.addData("username", user.getUsername());
        sendRequest(printBlocked);
        Response response = getResponse();
        System.out.println((String) response.getData());
        return response;
    }

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

    private Response checkUsername(String username) {
        Request<String> request = new Request<>(RequestType.CHECK_USERNAME);
        request.addData("username", username);
        sendRequest(request);
        return getResponse();
    }

    private void completeSignUp(String username, String pass, String email, String phoneNum) {
        makeSignUpRequest(username, pass, email, phoneNum);
        Response response = getResponse();
        if (response.getResponseStatus() == ResponseStatus.SIGNUP_VALID) {
            System.out.println("successfully signed up");
        }
    }

    private void makeSignUpRequest(String username, String pass, String email, String phoneNum) {
        Request<String> request = new Request<>(RequestType.SIGN_UP);
        request.addData("username", username);
        request.addData("password", pass);
        request.addData("email", email);
        request.addData("phoneNum", phoneNum);
        sendRequest(request);
    }

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

//    private void printFriends() {
//        Request<String> printFriends = new Request<>(RequestType.PRINT_FRIENDS);
//        printFriends.addData("username", user.getUsername());
//        sendRequest(printFriends);
//        Response response = getResponse();
//        System.out.println((String) response.getData());
//    }

    private void printList(RequestType requestType) {
        Request<String> printList = new Request<>(requestType);
        printList.addData("username", user.getUsername());
        sendRequest(printList);
        Response response = getResponse();
        System.out.println((String) response.getData());
    }

    private void printPrivateChatsUsernames() {
        Request<String> print = new Request<>(RequestType.PRINT_PRIVATE_CHATS_USERNAMES);
        print.addData("username", user.getUsername());
        sendRequest(print);
        Response response = getResponse();

        System.out.println((String) response.getData());
    }

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

    public void sendMessage(String channelIndex, String serverIndex, RequestType requestType){
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

    public void sendPrivateChatMessage(String username, String text) {
        Request<String> request = new Request<>(RequestType.SEND_MESSAGE);
        request.addData("message", text);
        request.addData("username", username);
        sendRequest(request);
    }

    public void sendChannelMessage(String channelIndex, String serverIndex, String text){
        Request<String> request = new Request<>(RequestType.SEND_MESSAGE_TO_CHANNEL);
        request.addData("message", text);
        request.addData("channelIndex", channelIndex);
        request.addData("serverIndex", serverIndex);
        sendRequest(request);
    }

    public void listenForMessage(Chat chat) {
        if (!this.user.isInThisChat(chat))
            return;
        new Thread(new Runnable() {
            @Override
            public void run() {
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
                    if (!messageContent.contains("sends a file."))
                        System.out.println(messageContent.substring(0, messageContent.indexOf(":")) + " is typing...");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(message);
                }
            }
        }).start();
    }

    public void downloadFile(File file) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket("localhost", 4321);
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
            }
        }).start();

    }

}