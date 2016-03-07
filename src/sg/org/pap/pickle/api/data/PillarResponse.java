package sg.org.pap.pickle.api.data;

import java.util.HashMap;
import java.util.Map;
import sg.org.pap.pickle.models.Pillar;

public class PillarResponse {
    private Map<String, Object> additionalProperties = new HashMap();
    private Pillar pillar;

    public Pillar getPillar() {
        return this.pillar;
    }

    public void setPillar(Pillar pillar) {
        this.pillar = pillar;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
