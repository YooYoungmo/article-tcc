package ymyoo.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ymyoo.payment.entity.ReservedPayment;

public interface ReservedPaymentRepository extends JpaRepository<ReservedPayment, Long> {
}
