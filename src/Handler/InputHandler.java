package Handler;

import java.util.InputMismatchException;
import java.util.Scanner;

import Exceptions.OutOfBoundsIndexException;

public class InputHandler {
    private static final Scanner scanner = new Scanner(System.in);

    /**
     * When getting an int in this class, there is a menu with a select
     * number of choices that the user can choose from. In order to handle
     * this, There is a parameter 'restriction' that defines a certain range
     * that the user can choose from. This method only ends if run successfully
     *
     * @param prompt      String prompt printed for the int to be entered by UserFeatures.User
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
                scanner.nextLine();

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
     * @throws InputMismatchException    for when a String is entered
     * @throws OutOfBoundsIndexException for when the int entered is not in the menu
     */
    private static int getIntFromUser(int indexRestriction) throws InputMismatchException, OutOfBoundsIndexException {
        int input;
        input = scanner.nextInt();
        if (input > indexRestriction)
            throw new OutOfBoundsIndexException("This message is not in the choices");
        scanner.nextLine();
        return input;
    }

    /**
     * prints a prompt and returns the string entered by user
     *
     * @param prompt string
     * @return string
     */
    public static String getString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    /**
     * Gets input from user based on specific regex of the desired type
     *
     * @param type type of input that is to be accepted from the user
     * @return the user input
     */
    public static String getInfo(String type) {
        String info = "";
        String regex = "";
        switch (type) {
            case "username" -> {
                info = getString("> Enter your desired username (must be at least 6 characters): ");
                regex = "^[a-zA-Z0-9].{5,}$";
            }
            case "pass" -> {
                info = getString("> Enter your desired password (must contain at least 1 capital letter, 1 number, and 8 characters): ");
                regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}$";
            }
            case "email" -> {
                info = getString("> Email: ");
                regex = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                        + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
            }
            case "phoneNum" -> {
                info = getString("> PhoneNumber (optional): ");
                regex = "(^\\d{10,11}+$|^$|^\\s$)";
            }
        }
        try {
            if (!info.matches(regex) && !info.equals("0"))
                throw new IllegalArgumentException();
            return info;
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid.");
        }
        return null;
    }

    /**
     * Checks if user input is equal to the input parameter
     *
     * @param s string that the user input is being compared to
     * @return RequestStatus based on whether the input was equal to the input parameter or not
     */
    public static RequestStatus checkInfo(String s) {
        if (s == null) {
            System.out.println("Try again");
            return RequestStatus.INVALID;
        }
        if (s.equals("0"))
            return RequestStatus.BACK;
        return RequestStatus.VALID;
    }
}
