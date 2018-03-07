package ymyoo.stock.entity;

import ymyoo.stock.AdjustmentType;
import ymyoo.stock.Status;

import javax.persistence.*;

@Entity
public class ReservedStock {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Enumerated(EnumType.STRING)
    private AdjustmentType adjustmentType;

    private String productId;

    private Long qty;

    @Enumerated(EnumType.STRING)
    private Status status;

    public ReservedStock() {
    }

    public ReservedStock(AdjustmentType adjustmentType, String productId, Long qty, Status status) {
        this.adjustmentType = adjustmentType;
        this.productId = productId;
        this.qty = qty;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public AdjustmentType getAdjustmentType() {
        return adjustmentType;
    }

    public String getProductId() {
        return productId;
    }

    public Long getQty() {
        return qty;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ReservedStock{" +
                "id=" + id +
                ", adjustmentType=" + adjustmentType +
                ", productId='" + productId + '\'' +
                ", qty=" + qty +
                ", status=" + status +
                '}';
    }
}
