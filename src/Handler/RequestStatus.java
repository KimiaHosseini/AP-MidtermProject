package Handler;

/**
 * the request status represents whether the validity of
 * the clients request and how it was handled.
 * The enum value varies between valid and invalid, as well
 * as back which invokes the method to end.
 */
public enum RequestStatus {
    VALID,
    INVALID,
    BACK,
}
