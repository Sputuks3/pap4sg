package sg.org.pap.pickle.models;

import java.util.HashMap;
import java.util.Map;

public class Message {
    public static final String TYPE_AFTERNOON = "6";
    public static final String TYPE_EVENING = "7";
    public static final String TYPE_HQ_CONTACT = "2";
    public static final String TYPE_MORNING = "5";
    public static final String TYPE_PDPA = "1";
    public static final String TYPE_TERMS_OF_USE = "3";
    private Map<String, Object> additionalProperties = new HashMap();
    private String content;
    private String id;
    private Object ophoto;
    private String photo;
    private String title;
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

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPhoto() {
        return this.photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }
}
