package ymyoo.order.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ymyoo.order.Order;
import ymyoo.order.adapter.ParticipantLink;
import ymyoo.order.adapter.TccRestAdapter;
import ymyoo.order.service.OrderService;

import java.util.HashMap;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private TccRestAdapter tccRestAdapter;

    @Autowired
    public void setTccRestAdapter(TccRestAdapter tccRestAdapter) {
        this.tccRestAdapter = tccRestAdapter;
    }

    @Override
    public void placeOrder(final Order order) {
        log.info("Place Order ...");

        // 1. 재고 차감(Try)
        ParticipantLink stockParticipantLink = reduceStock(order);

        // 2. 결제 요청(Try)
        ParticipantLink paymentParticipantLink = payOrder(order);

        // Exception Path : Failure Before Confirm
        if(order.getProductId().equals("prd-0002")) {
            log.info(String.format("Stock URI :%s", stockParticipantLink.getUri()));
            log.info(String.format("Payment URI :%s", paymentParticipantLink.getUri()));

            throw new RuntimeException("Error Before Confirm");
        }

        // 3. 트랜젝션 확정(Confirm)
        tccRestAdapter.confirmAll(stockParticipantLink.getUri(), paymentParticipantLink.getUri());

        log.info("End of place order");
    }

    private ParticipantLink reduceStock(final Order order) {
        final String requestURL = "http://localhost:8081/api/v1/stocks";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("adjustmentType", "REDUCE");
        requestBody.put("productId", order.getProductId());
        requestBody.put("qty", order.getQty());

        return tccRestAdapter.reserve(requestURL, requestBody);
    }

    private ParticipantLink payOrder(final Order order) {
        final String requestURL = "http://localhost:8082/api/v1/payments";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("orderId", order.getOrderId());
        requestBody.put("paymentAmt", order.getPaymentAmt());

        return tccRestAdapter.reserve(requestURL, requestBody);
    }
}
