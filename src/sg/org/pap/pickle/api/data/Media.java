package sg.org.pap.pickle.api.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Media implements Serializable {
    private Map<String, Object> additionalProperties = new HashMap();
    private String thumbnail;
    private String type;
    private String video;

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getThumbnail() {
        return this.thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getVideo() {
        return this.video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
