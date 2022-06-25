package Exceptions;

/**
 * This exception is thrown whenever an answer choice is
 * chosen that is not in the options of the menu
 */
public class OutOfBoundsIndexException extends Exception{
    public OutOfBoundsIndexException(String message) {
        super(message);
    }
}