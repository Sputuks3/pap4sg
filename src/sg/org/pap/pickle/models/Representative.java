package sg.org.pap.pickle.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Representative implements Serializable {
    private Map<String, Object> additionalProperties = new HashMap();
    private String constituencyAddress;
    private String constituencyId;
    private String constituencyName;
    private String constituencyPhoto1;
    private String constituencyPhoto2;
    private String contentType;
    private String description;
    private String designation;
    private String isFeatured;
    private String name;
    private String photo1;
    private String photo2;
    private String rid;
    private String townName;
    private String videoThumbnail;
    private String videoUrl;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoto1() {
        return this.photo1;
    }

    public void setPhoto1(String photo1) {
        this.photo1 = photo1;
    }

    public String getPhoto2() {
        return this.photo2;
    }

    public void setPhoto2(String photo2) {
        this.photo2 = photo2;
    }

    public String getDesignation() {
        return this.designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getConstituencyName() {
        return this.constituencyName;
    }

    public void setConstituencyName(String constituencyName) {
        this.constituencyName = constituencyName;
    }

    public String getConstituencyAddress() {
        return this.constituencyAddress;
    }

    public void setConstituencyAddress(String constituencyAddress) {
        this.constituencyAddress = constituencyAddress;
    }

    public String getConstituencyPhoto1() {
        return this.constituencyPhoto1;
    }

    public void setConstituencyPhoto1(String constituencyPhoto1) {
        this.constituencyPhoto1 = constituencyPhoto1;
    }

    public String getConstituencyPhoto2() {
        return this.constituencyPhoto2;
    }

    public void setConstituencyPhoto2(String constituencyPhoto2) {
        this.constituencyPhoto2 = constituencyPhoto2;
    }

    public String getRid() {
        return this.rid;
    }

    public void setRid(String rid) {
        this.rid = rid;
    }

    public String getTownName() {
        return this.townName;
    }

    public void setTownName(String townName) {
        this.townName = townName;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public String getContentType() {
        return this.contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getVideoThumbnail() {
        return this.videoThumbnail;
    }

    public void setVideoThumbnail(String videoThumbnail) {
        this.videoThumbnail = videoThumbnail;
    }

    public String getIsFeatured() {
        return this.isFeatured;
    }

    public void setIsFeatured(String isFeatured) {
        this.isFeatured = isFeatured;
    }

    public String getVideoUrl() {
        return this.videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getConstituencyId() {
        return this.constituencyId;
    }

    public void setConstituencyId(String constituencyId) {
        this.constituencyId = constituencyId;
    }
}
