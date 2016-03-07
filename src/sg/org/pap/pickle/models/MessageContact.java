package sg.org.pap.pickle.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MessageContact implements Serializable {
    private Map<String, Object> additionalProperties = new HashMap();
    private Content content;
    private String id;
    private Object ophoto;
    private String photo;
    private String type;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Content getContent() {
        return this.content;
    }

    public void setContent(Content content) {
        this.content = content;
    }

    public String getPhoto() {
        return this.photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public Object getOphoto() {
        return this.ophoto;
    }

    public void setOphoto(Object ophoto) {
        this.ophoto = ophoto;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
