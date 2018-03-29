package ymyoo.payment.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ymyoo.payment.Status;
import ymyoo.payment.dto.ParticipantLink;
import ymyoo.payment.dto.PaymentRequest;
import ymyoo.payment.entity.Payment;
import ymyoo.payment.entity.ReservedPayment;
import ymyoo.payment.repository.PaymentRepository;
import ymyoo.payment.repository.ReservedPaymentRepository;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentRestController {
    // 3초 타임 아웃
    private static final long TIMEOUT = TimeUnit.SECONDS.toMillis(3);

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
    public ResponseEntity<ParticipantLink> tryPayment(@RequestBody PaymentRequest paymentRequest) {
        ReservedPayment reservedPayment = new ReservedPayment(paymentRequest.getOrderId(), paymentRequest.getPaymentAmt());
        reservedPaymentRepository.save(reservedPayment);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(reservedPayment.getId()).toUri();
        final long expires = reservedPayment.getCreated().getTime() + TIMEOUT;

        return new ResponseEntity<>(new ParticipantLink(location, new Date(expires)), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> confirmPayment(@PathVariable Long id) {
        ReservedPayment reservedPayment = reservedPaymentRepository.findOne(id);

        final long confirmTime = System.currentTimeMillis();
        final long tryTime = reservedPayment.getCreated().getTime();

        final long duration = confirmTime - tryTime;

        log.info("duration : " + TimeUnit.MILLISECONDS.toSeconds(duration));
        if(duration > TIMEOUT) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        paymentRepository.save(new Payment(reservedPayment.getOrderId(), reservedPayment.getPaymentAmt()));
        paymentRepository.flush();

        reservedPayment.setStatus(Status.CONFIRMED);
        reservedPaymentRepository.save(reservedPayment);

        log.info("List of Payments");
        List<Payment> findAll = paymentRepository.findAll();
        findAll.forEach(findPayment -> log.info(findPayment.toString()));

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
