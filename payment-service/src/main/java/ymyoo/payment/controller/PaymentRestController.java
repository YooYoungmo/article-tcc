package ymyoo.payment.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ymyoo.payment.Status;
import ymyoo.payment.dto.PaymentRequest;
import ymyoo.payment.entity.Payment;
import ymyoo.payment.entity.ReservedPayment;
import ymyoo.payment.repository.PaymentRepository;
import ymyoo.payment.repository.ReservedPaymentRepository;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentRestController {

    private static final Logger log = LoggerFactory.getLogger(PaymentRestController.class);

    private PaymentRepository paymentRepository;

    private ReservedPaymentRepository reservedPaymentRepository;

    @Autowired
    public void setReservedPaymentRepository(ReservedPaymentRepository reservedPaymentRepository) {
        this.reservedPaymentRepository = reservedPaymentRepository;
    }

    @Autowired
    public void setPaymentRepository(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @PostMapping
    public ResponseEntity tryPayment(@RequestBody PaymentRequest paymentRequest) {
        ReservedPayment reservedPayment = new ReservedPayment(paymentRequest.getOrderId(), paymentRequest.getPaymentAmt(), Status.TRY);
        reservedPaymentRepository.save(reservedPayment);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(reservedPayment.getId()).toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> confirmPayment(@PathVariable Long id) {
        ReservedPayment reservedPayment = reservedPaymentRepository.findOne(id);

        paymentRepository.save(new Payment(reservedPayment.getOrderId(), reservedPayment.getPaymentAmt()));
        paymentRepository.flush();

        reservedPayment.setStatus(Status.CONFIRM);
        reservedPaymentRepository.save(reservedPayment);

        log.info("List of Payments");
        List<Payment> findAll = paymentRepository.findAll();
        findAll.forEach(findPayment -> log.info(findPayment.toString()));

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
