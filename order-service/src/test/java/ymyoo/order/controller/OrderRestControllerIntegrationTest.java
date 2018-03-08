package ymyoo.order.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderRestControllerIntegrationTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    public void placeOrder() {
        // given
        final String requestURL = "/api/v1/orders";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("productId", "prd-0001");
        requestBody.put("qty", 10);
        requestBody.put("paymentAmt", 20000);

        // when
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.postForEntity(requestURL, new HttpEntity(requestBody, headers), String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }
}