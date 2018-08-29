package ymyoo.payment.adapter.messaging.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import ymyoo.payment.adapter.messaging.PaymentOrderChannelAdapter;
import ymyoo.payment.dto.PaymentRequest;
import ymyoo.payment.service.PaymentService;

@Component
public class PaymentOrderChannelAdapterKafkaImpl implements PaymentOrderChannelAdapter {
    private static final Logger log = LoggerFactory.getLogger(PaymentOrderChannelAdapterKafkaImpl.class);

    private final String TOPIC = "payment-order";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private PaymentService paymentService;

    @Autowired
    public void setPaymentService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    public void publish(String message) {
        this.kafkaTemplate.send(TOPIC, message);
    }

    @KafkaListener(topics = TOPIC)
    public void subscribe(String message, Acknowledgment ack) {
        log.info(String.format("Message Received : %s", message));

        try {
            PaymentRequest paymentRequest = PaymentRequest.deserializeJSON(message);

            // 이미 처리된 주문인지 확인
            if(paymentService.isAlreadyProcessedOrderId(paymentRequest.getOrderId())) {
                log.info(String.format("AlreadyProcessedOrderId : [%s]", paymentRequest.getOrderId()));
            } else {
                paymentService.payOrder(paymentRequest.getOrderId(), paymentRequest.getPaymentAmt());
            }

            // Kafka Offset Manual Commit
            ack.acknowledge();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
