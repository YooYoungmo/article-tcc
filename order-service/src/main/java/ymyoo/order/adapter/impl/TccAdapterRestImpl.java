package ymyoo.order.adapter.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ymyoo.order.adapter.ParticipantLink;
import ymyoo.order.adapter.TccAdapter;

import java.net.URI;
import java.util.Map;

@Component
public class TccAdapterRestImpl implements TccAdapter {
    private static final Logger log = LoggerFactory.getLogger(TccAdapterRestImpl.class);

    private RestTemplate restTemplate = new RestTemplate();

    @Override
    public ParticipantLink doTry(final String requestURL, final Map<String, Object> requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<ParticipantLink> response = restTemplate.postForEntity(requestURL, new HttpEntity(requestBody, headers), ParticipantLink.class);

        if(response.getStatusCode() != HttpStatus.CREATED) {
            throw new RuntimeException(String.format("TRY Error[URI : %s][HTTP Status : %s]",
                    requestURL, response.getStatusCode().name()));
        }

        return response.getBody();
    }

    @Override
    public void confirm(final URI... uris) {
        for (URI uri : uris) {
            try {
                restTemplate.put(uri, null);
            } catch (RestClientException e) {
                cancel(uris);
                throw new RuntimeException(String.format("Confirm Error[URI : %s]",
                        uri.toString()), e);
            }
        }
    }

    private void cancel(final URI... uris) {
        for (URI uri : uris) {
            try {
                restTemplate.delete(uri);
            } catch (RestClientException e) {
                log.error(String.format("Cancel Error[URI : %s]", uri.toString()), e);
            }
        }
    }
}
