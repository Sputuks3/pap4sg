package sg.org.pap.pickle.api.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sg.org.pap.pickle.models.Pillar;

public class PillarsResponse {
    private Map<String, Object> additionalProperties = new HashMap();
    private Media media;
    private List<Pillar> pillars = new ArrayList();

    public List<Pillar> getPillars() {
        return this.pillars;
    }

    public void setPillars(List<Pillar> pillars) {
        this.pillars = pillars;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public Media getMedia() {
        return this.media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }
}
