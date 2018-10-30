package ymyoo.order.adapter.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ymyoo.order.adapter.ParticipantLink;
import ymyoo.order.adapter.ParticipationRequest;
import ymyoo.order.adapter.TccRestAdapter;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
public class TccRestAdapterImpl implements TccRestAdapter {
    private static final Logger log = LoggerFactory.getLogger(TccRestAdapterImpl.class);

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private RetryTemplate retryTemplate;

    @Override
    public List<ParticipantLink> doTry(List<ParticipationRequest> participationRequests) {
        List<ParticipantLink> participantLinks = new ArrayList<>();
        participationRequests.forEach(participationRequest -> {
            try {
                ParticipantLink participantLink = retryTemplate.execute((RetryCallback<ParticipantLink, RestClientException>) context -> {
                    URI uri = URI.create(participationRequest.getUrl());
                    RequestEntity<Map<String, Object>> request =
                        RequestEntity.post(uri)
                            .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON)
                            .body(participationRequest.getRequestBody());

                    ResponseEntity<ParticipantLink> response = restTemplate.exchange(request, ParticipantLink.class);
                    log.info(String.format("ParticipantLink URI :%s", response.getBody().getUri()));

                    return response.getBody();
                });

                participantLinks.add(participantLink);
            } catch (RestClientException e) {
                cancelAll(participantLinks);
                throw new RuntimeException(String.format("TCC - Try Error[URI : %s]", participationRequest.getUrl()), e);
            }
        });

        return participantLinks;
    }

    @Override
    public void cancelAll(List<ParticipantLink> participantLinks) {
        participantLinks.forEach(participantLink -> {
            try {
                retryTemplate.execute((RetryCallback<Void, RestClientException>) context -> {
                    restTemplate.delete(participantLink.getUri());
                    return null;
                });
            } catch (RestClientException e) {
                log.error(String.format("TCC - Cancel Error[URI : %s]", participantLink.getUri().toString()), e);
            }
        });
    }

    @Override
    public void confirmAll(List<ParticipantLink> participantLinks) {
        participantLinks.forEach(participantLink -> {
            try {
                retryTemplate.execute((RetryCallback<Void, RestClientException>) context -> {
                    restTemplate.put(participantLink.getUri(), null);
                    return null;
                });

            } catch (RestClientException e) {
                log.error(String.format("TCC - Confirm Error[URI : %s]", participantLink.getUri().toString()), e);
            }
        });
    }
}
