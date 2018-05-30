package ymyoo.stock.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ymyoo.stock.Status;
import ymyoo.stock.dto.StockAdjustment;

import javax.persistence.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Entity
public class ReservedStock {
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


    @Temporal(TemporalType.TIMESTAMP)
    private Date expires;

    public ReservedStock() {
    }

    public ReservedStock(StockAdjustment stockAdjustment) {
        try {
            this.resources = stockAdjustment.serializeJSON();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        this.created = new Date();
        this.expires = new Date(created.getTime() + TIMEOUT);
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

    public void validate(LocalDateTime confirmedTime) {
        validateStatus();
        validateExpired(confirmedTime);
    }

    private void validateStatus() {
        if(this.getStatus() == Status.CANCEL || this.getStatus() == Status.CONFIRMED) {
            throw new IllegalArgumentException("Invalidate Status");
        }
    }

    private void validateExpired(LocalDateTime confirmedTime) {
        LocalDateTime expiresTime = this.expires.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        if(confirmedTime.isAfter(expiresTime)) {
            throw new IllegalArgumentException("Expired");
        }
    }

    public Date getExpires() {
        return expires;
    }
}

