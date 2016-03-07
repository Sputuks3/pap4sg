package sg.org.pap.pickle.api.data;

import java.util.HashMap;
import java.util.Map;

public class Yyf {
    private Map<String, Object> additionalProperties = new HashMap();
    private Media media;
    private String webviewUrl;

    public Media getMedia() {
        return this.media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }

    public String getWebviewUrl() {
        return this.webviewUrl;
    }

    public void setWebviewUrl(String webviewUrl) {
        this.webviewUrl = webviewUrl;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
