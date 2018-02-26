package ymyoo.payment.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ymyoo.payment.dto.PaymentRequest;
import ymyoo.payment.entity.Payment;
import ymyoo.payment.repository.PaymentRepository;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private PaymentRepository paymentRepository;

    @Autowired
    public void setPaymentRepository(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @PostMapping
    public ResponseEntity<Void> pay(@RequestBody PaymentRequest paymentRequest) {
        paymentRepository.save(new Payment(paymentRequest.getOrderId(), paymentRequest.getPaymentAmt()));
        paymentRepository.flush();

        log.info("List of Payments");
        List<Payment> findAll = paymentRepository.findAll();
        findAll.forEach(findPayment -> log.info(findPayment.toString()));

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
