package ymyoo.stock.entity;

import javax.persistence.*;

@Entity
public class StockHistory {

  @Id
  @GeneratedValue(strategy= GenerationType.AUTO)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "stock_id")
  private Stock stock;

  private String adjustmentType;

  private Long qty;

  private String orderId;

  public StockHistory() {
  }

  public StockHistory(String adjustmentType, Long qty) {
    this.adjustmentType = adjustmentType;
    this.qty = qty;
  }

  public StockHistory(String adjustmentType, Long qty, String orderId) {
    this.adjustmentType = adjustmentType;
    this.qty = qty;
    this.orderId = orderId;
  }

  public Long getId() {
    return id;
  }

  public Stock getStock() {
    return stock;
  }

  public String getAdjustmentType() {
    return adjustmentType;
  }

  public Long getQty() {
    return qty;
  }

  public String getOrderId() {
    return orderId;
  }

  public void setStock(Stock stock) {
    this.stock = stock;
  }
}
