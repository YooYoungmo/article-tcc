package ymyoo.order.controller;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.rule.OutputCapture;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderRestControllerIntegrationTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Rule
    public OutputCapture outputCapture = new OutputCapture();

    @Test
    public void placeOrder() {
        // given
        final String requestURL = "/api/v1/orders";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("productId", "prd-0001");
        requestBody.put("qty", 10);
        requestBody.put("paymentAmt", 10000);

        // when
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.postForEntity(requestURL, new HttpEntity(requestBody, headers), String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    // If something fails, a polite workflow would explicitly cancel the successful reservations. - http://pautasso.info/talks/2014/wsrest/tcc/rest-tcc.html#/tcc-http-protocol-fail-cancel
    @Test
    public void placeOrder_TCC_TRY_단계에서_일부_마이크로_서비스가_실패하는_경우() {
        // given
        final String requestURL = "/api/v1/orders";

        // 결제 제한 금액 초과로 인해 재고 차감 try에는 성공하지만 결제 try 시 오류나는 경우를 만듬
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("productId", "prd-0001");
        requestBody.put("qty", 1);
        requestBody.put("paymentAmt", 300000);

        // when
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.postForEntity(requestURL, new HttpEntity(requestBody, headers), String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        List<String> uris = extractParticipantLinkURIs(outputCapture.toString());

        // Resources가 Cancel 되었는지 확인
        HttpHeaders confirmRequestHeader = new HttpHeaders();
        confirmRequestHeader.set("tcc-confirmed-time", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        uris.forEach(uri -> {
            ResponseEntity<String> confirmResponse = restTemplate.exchange(uri, HttpMethod.PUT, new HttpEntity(confirmRequestHeader), String.class);
            assertThat(confirmResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        });
    }

    // If something fails, do nothing. The reserved resources will eventually timeout. - http://pautasso.info/talks/2014/wsrest/tcc/rest-tcc.html#/tcc-http-protocol-fail-cancel
    @Test
    public void placeOrder_TCC_TRY는_모두_성공했지만_내부_오류로_인해_TCC_Confirm_하지_않는_경우() throws InterruptedException {
        // given
        final String requestURL = "/api/v1/orders";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("productId", "prd-0002");
        requestBody.put("qty", 1);
        requestBody.put("paymentAmt", 20000);

        // when
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.postForEntity(requestURL, new HttpEntity(requestBody, headers), String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        List<String> uris = extractParticipantLinkURIs(outputCapture.toString());

        // 타임 아웃 테스트를 위한 대기
        waitCurrentThread(5);

        // 타임 아웃 확인(TCC Timeout)
        HttpHeaders confirmRequestHeader = new HttpHeaders();
        confirmRequestHeader.set("tcc-confirmed-time", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        uris.forEach(uri -> {
            ResponseEntity<String> confirmResponse = restTemplate.exchange(uri, HttpMethod.PUT, new HttpEntity(confirmRequestHeader), String.class);
            assertThat(confirmResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        });
    }

    @Test
    public void placeOrder_TCC_TRY는_모두_성공했지만_내부_로직_수행_시간이_너무_오래_걸려_TCC_Confirm_중_TIMEOUT_되는_경우() {
        // given
        final String requestURL = "/api/v1/orders";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("productId", "prd-0003");
        requestBody.put("qty", 1);
        requestBody.put("paymentAmt", 40000);

        // when
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.postForEntity(requestURL, new HttpEntity(requestBody, headers), String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        // 타임 아웃 확인(TCC Timeout)
        List<String> uris = extractParticipantLinkURIs(outputCapture.toString());

        HttpHeaders confirmRequestHeader = new HttpHeaders();
        confirmRequestHeader.set("tcc-confirmed-time", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        uris.forEach(uri -> {
            ResponseEntity<String> confirmResponse = restTemplate.exchange(uri, HttpMethod.PUT, new HttpEntity(confirmRequestHeader), String.class);
            assertThat(confirmResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        });
    }

    private List<String> extractParticipantLinkURIs(String text) {
        String urlPrefix = "ParticipantLink URI :";
        List<String> containedUrls = new ArrayList<>();
        String urlRegex = urlPrefix + "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(text);

        while (urlMatcher.find()) {
            String url = text.substring(urlMatcher.start(0), urlMatcher.end(0));
            url = url.substring(urlPrefix.length(), url.length());

            containedUrls.add(url);
        }

        return containedUrls;
    }

    private void waitCurrentThread(int seconds) throws InterruptedException {
        Thread.currentThread().sleep(TimeUnit.SECONDS.toMillis(seconds));
    }
}