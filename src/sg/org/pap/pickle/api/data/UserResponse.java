package sg.org.pap.pickle.api.data;

import java.util.HashMap;
import java.util.Map;
import sg.org.pap.pickle.models.User;

public class UserResponse {
    private Map<String, Object> additionalProperties = new HashMap();
    private User user;

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
