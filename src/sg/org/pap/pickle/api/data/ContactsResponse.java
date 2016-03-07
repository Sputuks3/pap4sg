package sg.org.pap.pickle.api.data;

import java.util.HashMap;
import java.util.Map;
import sg.org.pap.pickle.models.MessageContact;

public class ContactsResponse {
    private Map<String, Object> additionalProperties = new HashMap();
    private MessageContact message;

    public MessageContact getMessage() {
        return this.message;
    }

    public void setMessage(MessageContact message) {
        this.message = message;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
