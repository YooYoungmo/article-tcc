package ymyoo.order.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ymyoo.order.Order;

import java.net.URI;

@Service
public class OrderServiceImpl implements OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Override
    public void placeOrder(final Order order) {
        log.info("Place Order ...");

        // 1. 재고 차감(Try)
        URI stockURI = tryStockReduction(order);

        // 2. 결제 요청(Try)
        URI paymentURI = tryPayment(order);

        // TODO 구매 주문 생성

        // 트랜젝션 확정(Confirm)
        confirmStockReduction(stockURI);
        confirmPayment(paymentURI);


        log.info("End of place order");
    }

    private URI tryStockReduction(final Order order) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final String requestURL = "http://localhost:8081/api/v1/stocks";
        final String requestBody = "{" +
                "\"adjustmentType\": \"REDUCE\"," +
                "\"productId\": \"" + order.getProductId() + "\"," +
                "\"qty\": " + order.getQty() +
                "}";

        ResponseEntity<String> response = restTemplate.postForEntity(requestURL, new HttpEntity(requestBody, headers), String.class);

        if(response.getStatusCode() != HttpStatus.CREATED) {
            throw new RuntimeException("재고 차감 요청 오류");
        }

        return response.getHeaders().getLocation();
    }

    private void confirmStockReduction(URI uri) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.PUT, new HttpEntity(headers), String.class);

        if(response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("재고 차감 확정 오류");
        }
    }

    private URI tryPayment(final Order order) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final String requestURL = "http://localhost:8082/api/v1/payments";
        final String requestBody = "{" +
                "\"orderId\": \"" + order.getOrderId() +"\"," +
                "\"paymentAmt\": " + order.getPaymentAmt() +
                "}";

        ResponseEntity<String> response = restTemplate.postForEntity(requestURL, new HttpEntity(requestBody, headers), String.class);

        if(response.getStatusCode() != HttpStatus.CREATED) {
            throw new RuntimeException("결제 요청 오류");
        }

        return response.getHeaders().getLocation();
    }

    private void confirmPayment(URI uri) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.PUT, new HttpEntity(headers), String.class);

        if(response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("결제 확정 오류");
        }
    }
}
