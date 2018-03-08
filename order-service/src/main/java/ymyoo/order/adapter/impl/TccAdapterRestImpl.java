package ymyoo.order.adapter.impl;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ymyoo.order.adapter.TccAdapter;

import java.net.URI;
import java.util.Map;

@Component
public class TccAdapterRestImpl implements TccAdapter {

    @Override
    public URI doTry(final String requestURL, final Map<String, Object> requestBody) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.postForEntity(requestURL, new HttpEntity(requestBody, headers), String.class);

        if(response.getStatusCode() != HttpStatus.CREATED) {
            throw new RuntimeException(String.format("TRY Error[URI : %s][HTTP Status : %s]",
                    requestURL, response.getStatusCode().name()));
        }

        return response.getHeaders().getLocation();
    }

    @Override
    public void confirm(final URI... uris) {
        for (URI uri : uris) {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.PUT, null, String.class);

            if(response.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException(String.format("Confirm Error[URI : %s][HTTP Status : %s]",
                        uri.toString(), response.getStatusCode().name()));
            }
        }
    }
}
