package ymyoo.order.adapter;

import java.net.URI;
import java.util.Map;

public interface TccRestAdapter {
    ParticipantLink reserve(final String requestURL, final Map<String, Object> requestBody);
    void confirmAll(URI... uris);
}
