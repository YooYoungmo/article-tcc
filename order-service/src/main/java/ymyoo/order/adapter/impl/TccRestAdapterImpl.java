package ymyoo.order.adapter.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ymyoo.order.adapter.ParticipantLink;
import ymyoo.order.adapter.ParticipationRequest;
import ymyoo.order.adapter.TccRestAdapter;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class TccRestAdapterImpl implements TccRestAdapter {
    private static final Logger log = LoggerFactory.getLogger(TccRestAdapterImpl.class);

    private RestTemplate restTemplate = new RestTemplate();

    @Override
    public List<ParticipantLink> doTry(List<ParticipationRequest> participationRequests) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        List<ParticipantLink> participantLinks = new ArrayList<>();

        participationRequests.forEach(participationRequest -> {
            try {
                ResponseEntity<ParticipantLink> response = restTemplate.postForEntity(participationRequest.getUrl(),
                        new HttpEntity(participationRequest.getRequestBody(), headers), ParticipantLink.class);

                log.info(String.format("ParticipantLink URI :%s", response.getBody().getUri()));
                participantLinks.add(response.getBody());
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
                restTemplate.delete(participantLink.getUri());
            } catch (RestClientException e) {
                log.error(String.format("TCC - Cancel Error[URI : %s]", participantLink.getUri().toString()), e);
            }
        });
    }

    @Override
    public void confirmAll(List<ParticipantLink> participantLinks) {
        final LocalDateTime confirmTime = LocalDateTime.now();

        validateTimeout(confirmTime, participantLinks);

        participantLinks.forEach(participantLink -> {
            try {
                URI uri = UriComponentsBuilder.fromUri(participantLink.getUri())
                        .queryParam("tcc-confirmed-time", confirmTime.format(DateTimeFormatter.ISO_DATE_TIME))
                        .build()
                        .toUri();

                restTemplate.put(uri, null);
            } catch (RestClientException e) {
                log.error(String.format("TCC - Confirm Error[URI : %s]", participantLink.getUri().toString()), e);
            }
        });
    }

    private void validateTimeout(LocalDateTime confirmTime, List<ParticipantLink> participantLinks) {
        participantLinks.forEach(participantLink -> {
            LocalDateTime expiresTime = participantLink.getExpires();

            if(confirmTime.isAfter(expiresTime)) {
                throw new RuntimeException(String.format("TCC - Confirm Error : TIMEOUT [URI:%s]", participantLink.getUri()));
            }
        });
    }
}
