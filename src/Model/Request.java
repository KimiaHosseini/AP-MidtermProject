package Model;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;

/**
 * This class is the data that is sent from client to server.
 * each request has a request type that determines how the server should
 * respond to it. it also contains data that might be needed in order for
 * the server to do the specific request
 *
 * @param <T> the type of data sent with the request
 */

public class Request<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 7769278356944487407L;
    private final RequestType requestType;
    private final HashMap<String, T> data;

    /**
     * Creates new request that contains the specific request type.
     *
     * @param requestType the type of request being made
     */
    public Request(RequestType requestType) {
        this.requestType = requestType;
        data = new HashMap<>();
    }

    /**
     * adds data to the request to be sent to the server
     *
     * @param key   the key is used in order to make each piece of data accessible and recognizable
     * @param value the value is the data added to the request, mapped by the key
     */
    public void addData(String key, T value) {
        if (data.containsKey(key))
            data.replace(key, value);
        else
            data.put(key, value);
    }

    /**
     * returns the data that is sent with the request
     *
     * @param key associated key that each piece of data is saved with
     * @return returns the data that has been mapped to the given key
     */
    public T getData(String key) {
        return data.get(key);
    }

    /**
     * returns the request type of the given request
     *
     * @return RequestType of the request
     */
    public RequestType getRequestType() {
        return requestType;
    }
}
