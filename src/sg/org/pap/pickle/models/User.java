package sg.org.pap.pickle.models;

import java.util.HashMap;
import java.util.Map;

public class User {
    private Map<String, Object> additionalProperties = new HashMap();
    private String constituencyId;
    private String constituencyName;
    private String constituencyPhoto;
    private String email;
    private String id;
    private String name;
    private String postalCode;
    private boolean subscription;
    private String townId;
    private String townName;
    private String townPhoto;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSubscription() {
        return this.subscription;
    }

    public void setSubscription(boolean subscription) {
        this.subscription = subscription;
    }

    public String getPostalCode() {
        return this.postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getConstituencyId() {
        return this.constituencyId;
    }

    public void setConstituencyId(String constituencyId) {
        this.constituencyId = constituencyId;
    }

    public String getConstituencyName() {
        return this.constituencyName;
    }

    public void setConstituencyName(String constituencyName) {
        this.constituencyName = constituencyName;
    }

    public String getConstituencyPhoto() {
        return this.constituencyPhoto;
    }

    public void setConstituencyPhoto(String constituencyPhoto) {
        this.constituencyPhoto = constituencyPhoto;
    }

    public String getTownId() {
        return this.townId;
    }

    public void setTownId(String townId) {
        this.townId = townId;
    }

    public String getTownName() {
        return this.townName;
    }

    public void setTownName(String townName) {
        this.townName = townName;
    }

    public String getTownPhoto() {
        return this.townPhoto;
    }

    public void setTownPhoto(String townPhoto) {
        this.townPhoto = townPhoto;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
