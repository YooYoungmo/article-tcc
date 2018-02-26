package ymyoo.payment.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    private String orderId;

    private Long paymentAmt;

    public Payment() {
    }

    public Payment(String orderId, Long paymentAmt) {
        this.orderId = orderId;
        this.paymentAmt = paymentAmt;
    }

    public Long getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public Long getPaymentAmt() {
        return paymentAmt;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", orderId='" + orderId + '\'' +
                ", paymentAmt=" + paymentAmt +
                '}';
    }
}
