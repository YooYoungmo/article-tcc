package ymyoo.payment.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ymyoo.payment.Status;
import ymyoo.payment.adapter.messaging.PaymentOrderChannelAdapter;
import ymyoo.payment.dto.PaymentRequest;
import ymyoo.payment.entity.Payment;
import ymyoo.payment.entity.ReservedPayment;
import ymyoo.payment.repository.PaymentRepository;
import ymyoo.payment.repository.ReservedPaymentRepository;
import ymyoo.payment.service.PaymentService;

import java.util.concurrent.TimeUnit;

@Service
public class PaymentServiceImpl implements PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    // 3초 타임 아웃
    public static final long TIMEOUT = TimeUnit.SECONDS.toMillis(3);

    private ReservedPaymentRepository reservedPaymentRepository;

    private PaymentRepository paymentRepository;

    private PaymentOrderChannelAdapter paymentOrderChannelAdapter;

    @Autowired
    public void setPaymentOrderChannelAdapter(PaymentOrderChannelAdapter paymentOrderChannelAdapter) {
        this.paymentOrderChannelAdapter = paymentOrderChannelAdapter;
    }

    @Autowired
    public void setReservedPaymentRepository(ReservedPaymentRepository reservedPaymentRepository) {
        this.reservedPaymentRepository = reservedPaymentRepository;
    }

    @Autowired
    public void setPaymentRepository(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public ReservedPayment reservePayment(PaymentRequest paymentRequest) {
        // 결제 가능한지 여부 검증
//        if(reservedPayment.getResources().getPaymentAmt() >= 300000) {
//            throw new RuntimeException("결제 제한 금액 초과");
//        }

        ReservedPayment reservedPayment = new ReservedPayment(paymentRequest);
        reservedPaymentRepository.save(reservedPayment);

        log.info("Reserved Payment :" + reservedPayment.getId());
        return reservedPayment;
    }

    @Transactional
    @Override
    public void confirmPayment(Long id) {
        ReservedPayment reservedPayment = reservedPaymentRepository.getOne(id);

        reservedPayment.validate();

        reservedPayment.setStatus(Status.CONFIRMED);
        reservedPaymentRepository.save(reservedPayment);

        paymentOrderChannelAdapter.publish(reservedPayment.getResources());

        log.info("Confirm Payment : " + id);
    }

    @Transactional
    @Override
    public void payOrder(final String orderId, final Long amount) {
        // Payment Gateway 연동 등등..
        // ...
        final Payment payment = new Payment(orderId, amount);
        paymentRepository.save(payment);

        log.info(String.format("Order payed ..[orderId : %s][amount  : %d]", orderId, amount));
    }

    @Transactional
    @Override
    public void cancelPayment(Long id) {
        ReservedPayment reservedPayment = reservedPaymentRepository.getOne(id);

        if(reservedPayment.getStatus() == Status.CONFIRMED) {
            // 이미 Confirm 되었다면..
            // 결제 취소...
        }

        reservedPayment.setStatus(Status.CANCEL);
        reservedPaymentRepository.save(reservedPayment);

        log.info("Cancel Payment : " + id);
    }
}
