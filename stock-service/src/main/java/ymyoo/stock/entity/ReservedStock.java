package ymyoo.stock.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import ymyoo.stock.Status;
import ymyoo.stock.dto.StockAdjustment;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
public class ReservedStock {
    // 3초 타임 아웃
    private static final long TIMEOUT = 3L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String resources;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime created;

    private LocalDateTime expires;

    public ReservedStock() {
    }

    public ReservedStock(StockAdjustment stockAdjustment) {
        try {
            this.resources = stockAdjustment.serializeJSON();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        this.created = LocalDateTime.now();
        this.expires = created.plus(TIMEOUT, ChronoUnit.SECONDS);
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

    public LocalDateTime getCreated() {
        return created;
    }

    public LocalDateTime getExpires() {
        return expires;
    }

    public void validate() {
        validateStatus();
        validateExpired();
    }

    public void validateStatus() {
        if(this.getStatus() == Status.CANCEL || this.getStatus() == Status.CONFIRMED) {
            throw new IllegalArgumentException("Invalidate Status");
        }
    }

    private void validateExpired() {
        if(LocalDateTime.now().isAfter(this.expires)) {
            throw new IllegalArgumentException("Expired");
        }
    }

}

