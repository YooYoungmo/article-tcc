package ymyoo.payment.entity;

import ymyoo.payment.Status;

import javax.persistence.*;
import java.util.Date;

@Entity
public class ReservedPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String orderId;

    private Long paymentAmt;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Temporal(TemporalType.TIMESTAMP)
    private Date created;


    public ReservedPayment() {
    }

    public ReservedPayment(String orderId, Long paymentAmt, Status status) {
        this.orderId = orderId;
        this.paymentAmt = paymentAmt;
        this.status = status;
        this.created = new Date();
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

    public Date getCreated() {
        return created;
    }

    @Override
    public String toString() {
        return "ReservedPayment{" +
                "id=" + id +
                ", orderId='" + orderId + '\'' +
                ", paymentAmt=" + paymentAmt +
                ", status=" + status +
                ", created=" + created +
                '}';
    }
}
