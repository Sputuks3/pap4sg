package sg.org.pap.pickle.api.data;

import java.io.Serializable;

public class Response implements Serializable {
    private String message;
    private String status;

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
