package Model;

import java.io.Serial;
import java.io.Serializable;
import DiscordFeatures.DiscordServer;
import Handler.ResponseStatus;

public class Response implements Serializable {
    @Serial
    private static final long serialVersionUID = 8552713475454448612L;
    private ResponseStatus responseStatus;
    private Object data;

    public Response(ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }

    public Response(ResponseStatus responseStatus, Object data){
        this.responseStatus = responseStatus;
        this.data = data;
    }
    public ResponseStatus getResponseStatus() {
        return responseStatus;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Object getData() {
        return data;
    }
}
