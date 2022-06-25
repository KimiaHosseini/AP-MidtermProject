package Model;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;

public class Request<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 7769278356944487407L;
    private RequestType requestType;
    private HashMap<String, T> data;

    public Request(RequestType requestType) {
        this.requestType = requestType;
        data = new HashMap<>();
    }

    public void addData(String key, T value){
        data.put(key, value);
    }

    public T getData(String key){
        return data.get(key);
    }

    public HashMap<String, T> getData() {
        return data;
    }

    public RequestType getRequestType() {
        return requestType;
    }
}
