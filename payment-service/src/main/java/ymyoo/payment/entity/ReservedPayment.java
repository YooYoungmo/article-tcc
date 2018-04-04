package ymyoo.payment.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ymyoo.payment.Status;
import ymyoo.payment.dto.PaymentRequest;

import javax.persistence.*;
import java.io.IOException;
import java.util.Date;

@Entity
public class ReservedPayment {
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
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            this.resources = objectMapper.writeValueAsString(paymentRequest);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        this.created = new Date();
    }

    public Long getId() {
        return id;
    }

    public PaymentRequest getResources() {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(this.resources, PaymentRequest.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
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

}
