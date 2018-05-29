package ymyoo.order.adapter;

import java.util.HashMap;
import java.util.Map;

public class ParticipationRequest {
    private String url;
    private Map<String, Object> requestBody = new HashMap<>();

    public ParticipationRequest(String url, Map<String, Object> requestBody) {
        this.url = url;
        this.requestBody = requestBody;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, Object> getRequestBody() {
        return requestBody;
    }
}
