package ymyoo.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ymyoo.payment.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Payment findByOrderId(String orderId);
}
