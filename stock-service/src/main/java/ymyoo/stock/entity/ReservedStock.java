package ymyoo.stock.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ymyoo.stock.Status;
import ymyoo.stock.dto.StockAdjustment;

import javax.persistence.*;
import java.io.IOException;
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

    public ReservedStock() {
    }

    public ReservedStock(StockAdjustment stockAdjustment) {
        try {
            this.resources = stockAdjustment.serializeJSON();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        this.created = new Date();
    }

    public Long getId() {
        return id;
    }

    public StockAdjustment getResourcesToObject() {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(this.resources, StockAdjustment.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
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
        if(this.getStatus() == Status.CANCEL) {
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

