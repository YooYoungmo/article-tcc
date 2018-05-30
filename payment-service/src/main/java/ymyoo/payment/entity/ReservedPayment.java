package ymyoo.payment.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import ymyoo.payment.Status;
import ymyoo.payment.dto.PaymentRequest;

import javax.persistence.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Entity
public class ReservedPayment {
    // 3초 타임 아웃
    private static final long TIMEOUT = TimeUnit.SECONDS.toMillis(3);

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String resources;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    public ReservedPayment() {
    }

    public ReservedPayment(PaymentRequest paymentRequest) {
        try {
            this.resources = paymentRequest.serializeJSON();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        this.created = new Date();
    }

    public Long getId() {
        return id;
    }

    public String getResources() {
        return this.resources;
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

    public void validate() {
        validateStatus();
        validateExpired();
    }

    private void validateStatus() {
        if(this.getStatus() == Status.CANCEL || this.getStatus() == Status.CONFIRMED) {
            throw new IllegalArgumentException("Invalidate Status");
        }
    }

    private void validateExpired() {
        final long confirmTime = System.currentTimeMillis();
        final long reservedTime = this.created.getTime();

        final long duration = confirmTime - reservedTime;

        if(duration > TIMEOUT) {
            throw new IllegalArgumentException("Expired");
        }
    }

    public long getTimeout() {
        return TIMEOUT;
    }
}
