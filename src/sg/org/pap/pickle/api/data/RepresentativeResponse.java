package sg.org.pap.pickle.api.data;

import java.util.HashMap;
import java.util.Map;
import sg.org.pap.pickle.models.RepresentativeDetails;

public class RepresentativeResponse {
    private Map<String, Object> additionalProperties = new HashMap();
    private RepresentativeDetails representative;

    public RepresentativeDetails getRepresentative() {
        return this.representative;
    }

    public void setRepresentative(RepresentativeDetails representative) {
        this.representative = representative;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
