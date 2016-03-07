package sg.org.pap.pickle.api.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sg.org.pap.pickle.models.Representative;

public class RepsListResponse implements Serializable {
    private Map<String, Object> additionalProperties = new HashMap();
    private List<Representative> representatives = new ArrayList();

    public List<Representative> getRepresentatives() {
        return this.representatives;
    }

    public void setRepresentatives(List<Representative> representatives) {
        this.representatives = representatives;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
