package sg.org.pap.pickle.api.data;

import java.util.HashMap;
import java.util.Map;
import sg.org.pap.pickle.models.News;

public class NewsResponse {
    private Map<String, Object> additionalProperties = new HashMap();
    private News news;

    public News getNews() {
        return this.news;
    }

    public void setNews(News news) {
        this.news = news;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
