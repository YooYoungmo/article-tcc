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

@Service
public class PaymentServiceImpl implements PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

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
        // 유효성 검사
        paymentRequest.validate();

        ReservedPayment reservedPayment = new ReservedPayment(paymentRequest);
        reservedPaymentRepository.save(reservedPayment);

        log.info("Reserved Payment :" + reservedPayment.getId());
        return reservedPayment;
    }

    @Transactional
    @Override
    public void confirmPayment(final Long id) {
        ReservedPayment reservedPayment = reservedPaymentRepository.getOne(id);

        if(reservedPayment == null) {
            throw new IllegalArgumentException("Not found");
        }

        reservedPayment.validate();

        reservedPayment.setStatus(Status.CONFIRMED);
        reservedPaymentRepository.save(reservedPayment);

        paymentOrderChannelAdapter.publish(reservedPayment.getResources());
        log.info("Confirmed Payment : " + id);
    }

    @Transactional
    @Override
    public void payOrder(final String orderId, final Long amount) {
        // Payment Gateway 연동 등등..
        // ...
        final Payment payment = new Payment(orderId, amount);
        paymentRepository.save(payment);

        log.info(String.format("Paid Order..[orderId : %s][amount  : %d]", orderId, amount));
    }

    @Transactional
    @Override
    public void cancelPayment(final Long id) {
        ReservedPayment reservedPayment = reservedPaymentRepository.getOne(id);
        reservedPayment.setStatus(Status.CANCEL);
        reservedPaymentRepository.save(reservedPayment);

        log.info("Canceled Payment : " + id);
    }

    @Override
    public boolean isAlreadyProcessedOrderId(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId);

        if(payment == null) {
            return false;
        } else {
            return true;
        }
    }
}
