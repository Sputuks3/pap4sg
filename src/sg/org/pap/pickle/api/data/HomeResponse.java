package sg.org.pap.pickle.api.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sg.org.pap.pickle.models.News;
import sg.org.pap.pickle.models.Representative;

public class HomeResponse implements Serializable {
    private Map<String, Object> additionalProperties = new HashMap();
    private String constituencyPhoto;
    private List<News> news = new ArrayList();
    private List<Representative> representatives = new ArrayList();

    public List<Representative> getRepresentatives() {
        return this.representatives;
    }

    public void setRepresentatives(List<Representative> representatives) {
        this.representatives = representatives;
    }

    public List<News> getNews() {
        return this.news;
    }

    public void setNews(List<News> news) {
        this.news = news;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public String getConstituencyPhoto() {
        return this.constituencyPhoto;
    }

    public void setConstituencyPhoto(String constituencyPhoto) {
        this.constituencyPhoto = constituencyPhoto;
    }
}
