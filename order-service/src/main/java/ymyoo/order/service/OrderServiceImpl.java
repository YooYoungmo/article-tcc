package ymyoo.order.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ymyoo.order.Order;

@Service
public class OrderServiceImpl implements OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Override
    public void placeOrder(final Order order) {
        log.info("Place Order ...");

        // 1. 재고 차감
        reduceStock(order);

        // 2. 결제 요청
        payOrder(order);

        log.info("End of place order");
    }

    private void reduceStock(final Order order) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final String STOCK_API_BASE_URL = "http://localhost:8081/api/v1/";
        final String requestURL = STOCK_API_BASE_URL + "products/" + order.getProductId();
        final String requestBody = "{\"adjustmentType\":\"REDUCE\",\"qty\": " + order.getQty() + "}";

        ResponseEntity<String> response = restTemplate.exchange(requestURL, HttpMethod.PUT, new HttpEntity(requestBody, headers), String.class);

        if(response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("재고 차감 오류");
        }
    }

    private void payOrder(final Order order) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final String PAYMENT_API_BASE_URL = "http://localhost:8082/api/v1/";
        final String requestURL = PAYMENT_API_BASE_URL + "payments";
        final String requestBody = "{\"orderId\":\"" + order.getOrderId() + "\" ,\"paymentAmt\": " + order.getPaymentAmt() + "}";
        ResponseEntity<String> response = restTemplate.postForEntity(requestURL, new HttpEntity(requestBody, headers), String.class);

        if(response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("결제 오류");
        }
    }
}
