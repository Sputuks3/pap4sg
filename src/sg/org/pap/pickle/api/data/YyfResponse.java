package sg.org.pap.pickle.api.data;

import java.util.HashMap;
import java.util.Map;

public class YyfResponse {
    private Map<String, Object> additionalProperties = new HashMap();
    private Yyf yyf;

    public Yyf getYyf() {
        return this.yyf;
    }

    public void setYyf(Yyf yyf) {
        this.yyf = yyf;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
