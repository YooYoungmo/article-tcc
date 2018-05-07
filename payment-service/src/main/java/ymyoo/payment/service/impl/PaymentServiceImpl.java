package ymyoo.payment.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ymyoo.payment.Status;
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

    @Autowired
    public void setReservedPaymentRepository(ReservedPaymentRepository reservedPaymentRepository) {
        this.reservedPaymentRepository = reservedPaymentRepository;
    }

    @Autowired
    public void setPaymentRepository(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public ReservedPayment tryPayment(PaymentRequest paymentRequest) {
        ReservedPayment reservedPayment = new ReservedPayment(paymentRequest);
        reservedPaymentRepository.save(reservedPayment);

        log.info("Reserved Payment :" + reservedPayment.getId());
        return reservedPayment;
    }

    @Transactional
    @Override
    public void confirmPayment(Long id) {
        ReservedPayment reservedPayment = reservedPaymentRepository.findOne(id);

        validateReservedPayment(reservedPayment);

        // Exception Path..
        if(reservedPayment.getResources().getPaymentAmt() >= 300000) {
            throw new RuntimeException("결제 제한 금액 초과");
        }

        paymentRepository.save(new Payment(reservedPayment.getResources().getOrderId(), reservedPayment.getResources().getPaymentAmt()));

        reservedPayment.setStatus(Status.CONFIRMED);
        reservedPaymentRepository.save(reservedPayment);

        log.info("Confirm Payment : " + id);
    }

    private void validateReservedPayment(ReservedPayment reservedPayment) {
        validateStatus(reservedPayment);
        validateExpired(reservedPayment);
    }

    private void validateStatus(ReservedPayment reservedPayment) {
        if (reservedPayment.getStatus() == Status.CANCEL) {
            throw new IllegalArgumentException("Invalidate Status");
        }
    }

    private void validateExpired(ReservedPayment reservedPayment) {
        final long confirmTime = System.currentTimeMillis();
        final long reservedTime = reservedPayment.getCreated().getTime();

        final long duration = confirmTime - reservedTime;

        if(duration > TIMEOUT) {
            throw new IllegalArgumentException("Expired");
        }
    }

    @Transactional
    @Override
    public void cancelPayment(Long id) {
        ReservedPayment reservedPayment = reservedPaymentRepository.findOne(id);

        if(reservedPayment.getStatus() == Status.CONFIRMED) {
            // 이미 Confirm 되었다면..
            // 결제 취소...
        }

        reservedPayment.setStatus(Status.CANCEL);
        reservedPaymentRepository.save(reservedPayment);

        log.info("Cancel Payment : " + id);
    }
}
