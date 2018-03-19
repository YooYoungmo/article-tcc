package ymyoo.order.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ymyoo.order.Order;
import ymyoo.order.adapter.TccAdapter;
import ymyoo.order.service.OrderService;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private TccAdapter tccAdapter;

    @Autowired
    public void setTccAdapter(TccAdapter tccAdapter) {
        this.tccAdapter = tccAdapter;
    }

    @Override
    public void placeOrder(final Order order) {
        log.info("Place Order ...");

        // 1. 재고 차감(Try)
        URI stockURI = reduceStock(order);

        // 2. 결제 요청(Try)
        URI paymentURI = payOrder(order);

        if(order.getProductId().equals("prd-0002")) {
            // Exception Path : Failure Before Confirm
            log.info(String.format("Stock URI :%s", stockURI));
            log.info(String.format("Payment URI :%s", paymentURI));

            throw new RuntimeException("Error Before Confirm");
        }

        // 3. 트랜젝션 확정(Confirm)
        tccAdapter.confirm(stockURI, paymentURI);

        log.info("End of place order");
    }

    private URI reduceStock(final Order order) {
        final String requestURL = "http://localhost:8081/api/v1/stocks";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("adjustmentType", "REDUCE");
        requestBody.put("productId", order.getProductId());
        requestBody.put("qty", order.getQty());

        return tccAdapter.doTry(requestURL, requestBody);
    }

    private URI payOrder(final Order order) {
        final String requestURL = "http://localhost:8082/api/v1/payments";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("orderId", order.getOrderId());
        requestBody.put("paymentAmt", order.getPaymentAmt());

        return tccAdapter.doTry(requestURL, requestBody);
    }
}
