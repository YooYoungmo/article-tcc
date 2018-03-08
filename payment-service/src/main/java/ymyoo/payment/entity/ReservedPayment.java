package ymyoo.payment.entity;

import ymyoo.payment.Status;

import javax.persistence.*;

@Entity
public class ReservedPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String orderId;

    private Long paymentAmt;

    @Enumerated(EnumType.STRING)
    private Status status;

    public ReservedPayment() {
    }

    public ReservedPayment(String orderId, Long paymentAmt, Status status) {
        this.orderId = orderId;
        this.paymentAmt = paymentAmt;
        this.status = status;
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }


    @Override
    public String toString() {
        return "ReservedPayment{" +
                "id=" + id +
                ", orderId='" + orderId + '\'' +
                ", paymentAmt=" + paymentAmt +
                ", status=" + status +
                '}';
    }
}
