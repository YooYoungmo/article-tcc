package ymyoo.order.adapter;

import java.net.URI;
import java.util.Map;

public interface TccAdapter {
    ParticipantLink doTry(final String requestURL, final Map<String, Object> requestBody);
    void confirm(URI... uris);
}
