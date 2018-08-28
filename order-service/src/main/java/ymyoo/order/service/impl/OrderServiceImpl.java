package ymyoo.order.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ymyoo.order.Order;
import ymyoo.order.adapter.ParticipantLink;
import ymyoo.order.adapter.ParticipationRequest;
import ymyoo.order.adapter.TccRestAdapter;
import ymyoo.order.service.OrderService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

        // REST Resources 생성
        // 재고 차감 요청
        ParticipationRequest stockParticipationRequest = reduceStock(order);

        // 결제 요청
        ParticipationRequest paymentParticipationRequest = payOrder(order);

        // 1. TCC - Try
        List<ParticipantLink> participantLinks  =
                tccRestAdapter.doTry(Arrays.asList(stockParticipationRequest, paymentParticipationRequest));

        // Exception Path
        // ymyoo.order.controller.OrderRestControllerIntegrationTest.placeOrder_TCC_TRY는_모두_성공했지만_내부_오류로_인해_TCC_Confirm_하지_않는_경우
        if(order.getProductId().equals("prd-0002")) {
            throw new RuntimeException("Error Before Confirm...");
        }

        // Exception Path
        // ymyoo.order.controller.OrderRestControllerIntegrationTest.placeOrder_TCC_TRY는_모두_성공했지만_내부_오류_후_명시적으로_Cancel_하는_경우
        if(order.getProductId().equals("prd-0004")) {
            tccRestAdapter.cancelAll(participantLinks);
            throw new RuntimeException("Error Before Confirm...");
        }

        // 2. TCC - Confirm
        tccRestAdapter.confirmAll(participantLinks);

        log.info("End of place order");
    }

    private ParticipationRequest reduceStock(final Order order) {
        final String requestURL = "http://localhost:8081/api/v1/stocks";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("orderId", order.getOrderId());
        requestBody.put("adjustmentType", "REDUCE");
        requestBody.put("productId", order.getProductId());
        requestBody.put("qty", order.getQty());

        return new ParticipationRequest(requestURL, requestBody);
    }

    private ParticipationRequest payOrder(final Order order) {
        final String requestURL = "http://localhost:8082/api/v1/payments";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("orderId", order.getOrderId());
        requestBody.put("paymentAmt", order.getPaymentAmt());

        return new ParticipationRequest(requestURL, requestBody);
    }

    private void waitCurrentThread(int seconds) throws InterruptedException {
        Thread.currentThread().sleep(TimeUnit.SECONDS.toMillis(seconds));
    }
}
