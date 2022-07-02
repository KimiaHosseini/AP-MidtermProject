package Model;

import java.io.Serial;
import java.io.Serializable;

import Handler.ResponseStatus;

/**
 * This class is the data that is sent from server to client.
 * each response has a response status that determines whether
 * the request was handled successfully or not in the server.
 * The response may also contain requested data.
 */

public class Response implements Serializable {
    @Serial
    private static final long serialVersionUID = 8552713475454448612L;
    private final ResponseStatus responseStatus;
    private Object data;

    /**
     * Creates new response that contains the specific response type.
     *
     * @param responseStatus the type of response being made
     */
    public Response(ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }

    /**
     * Creates new response that contains the specific response type
     * as well as requested data by the client
     *
     * @param responseStatus the type of response being made
     * @param data           any data that has been requested by the client
     */
    public Response(ResponseStatus responseStatus, Object data) {
        this.responseStatus = responseStatus;
        this.data = data;
    }

    /**
     * returns the response status of the given request
     *
     * @return ResponseStatus of the response
     */
    public ResponseStatus getResponseStatus() {
        return responseStatus;
    }

    /**
     * returns the data that is sent with the response
     *
     * @return returns the data that has been saved to the response
     */
    public Object getData() {
        return data;
    }
}
