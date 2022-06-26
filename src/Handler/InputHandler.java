package Handler;

import java.util.InputMismatchException;
import java.util.Scanner;
import Exceptions.OutOfBoundsIndexException;

public class InputHandler {

    private static Scanner scanner = new Scanner(System.in);

    /**
     * When getting an int in this class, there is a menu with a select
     * number of choices that the user can choose from. In order to handle
     * this, There is a parameter 'restriction' that defines a certain range
     * that the user can choose from. This method only ends if run successfully
     *
     * @param prompt String prompt printed for the int to be entered by UserFeatures.User
     * @param restriction restricting the user to a maximum they can choose from
     * @return int valid input
     */
    public static int getInt(String prompt, int restriction) {
        boolean continueLoop = true;
        int input = 0;
        do {
            try {
                System.out.println(prompt);
                input = getIntFromUser(restriction);
                continueLoop = false;
            } catch (OutOfBoundsIndexException outOfBoundsIndexException) {
                System.out.println("The integer you entered is not an option. Please try again.");
            } catch (InputMismatchException inputMismatchException) {
                System.out.println("You must enter an integer. Please try again.");
            }
        } while (continueLoop);

        return input;
    }


    /**
     * prints a prompt and
     * gets int from user. Throws exceptions for when it is not
     * within the restriction or when a String is entered
     *
     * @param indexRestriction int
     * @return returns the int submitted
     * @throws InputMismatchException for when a String is entered
     * @throws OutOfBoundsIndexException for when the int entered is not in the menu
     */
    private static int getIntFromUser(int indexRestriction) throws InputMismatchException, OutOfBoundsIndexException {
        int input = 0;
        input = scanner.nextInt();
            if (input > indexRestriction)
                throw new OutOfBoundsIndexException("This message is not in the choices");
        scanner.nextLine();
        return input;
    }

    /**
     * prints a prompt and returns the string entered by user
     * @param prompt string
     * @return string
     */
    public static String getString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    /**
     * gets String until # is entered
     * @param prompt prompt printed for user input
     * @return string
     */
    public static String getParagraph(String prompt) {
        System.out.println(prompt);
        String body = "";
        String temp = "";
        do {
            temp = scanner.nextLine();
            if (!temp.equals("#")) {
                body = body.concat(temp);
            }
        } while (!temp.equals("#"));
        return body;
    }

    public static String getInfo(String type){
        String info = "";
        String regex = "";
        switch (type){
            case "username":{
                info =  getString("> Username(...): ");
                regex = "^[a-zA-Z0-9].{5,}$";
                break;
            }
            case "pass":{
                info = getString("> Password(....): ");
                regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}$";
                break;
            }
            case "email":{
                info = getString("> Email: ");
                regex = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                        + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
                break;
            }
            case "phoneNum":{
                info = getString("> PhoneNumber(optional): ");
                regex = "(^\\d{10,11}+$|^$|^\\s$)";
            }
        }
        try {
            if (!info.matches(regex) && !info.equals("0"))
                throw new IllegalArgumentException();
            return info;
        }catch (IllegalArgumentException e){
            System.out.print("Invalid ");
        }
        return null;
    }

    public static RequestStatus checkInfo(String s){
        if (s == null){
            System.out.println("Try again");
            return RequestStatus.INVALID;
        }
        if (s.equals("0"))
            return RequestStatus.BACK;
        return RequestStatus.VALID;
    }
}

