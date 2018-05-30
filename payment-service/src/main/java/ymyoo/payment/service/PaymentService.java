package ymyoo.payment.service;

import ymyoo.payment.dto.PaymentRequest;
import ymyoo.payment.entity.ReservedPayment;

import java.time.LocalDateTime;

public interface PaymentService {
    ReservedPayment reservePayment(PaymentRequest paymentRequest);

    void confirmPayment(Long id, LocalDateTime confirmedTime);

    void cancelPayment(Long id);

    void payOrder(String orderId, Long amount);
}
