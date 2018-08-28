package ymyoo.stock.entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Stock {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @Column(name = "stock_id")
    private Long id;

    private String productId;

    private Long availableStockQty;

    @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL)
    private List<StockHistory> stockHistories = new ArrayList<>();

    public Stock() {
    }

    public Stock(String productId, Long availableStockQty) {
        this.productId = productId;
        this.availableStockQty = availableStockQty;

        this.addHistory(new StockHistory("INITIAL", availableStockQty));
    }

    public Long getId() {
        return id;
    }

    public String getProductId() {
        return productId;
    }

    public Long getAvailableStockQty() {
        return availableStockQty;
    }

    public void decrease(final String orderId, final Long qty) {
        this.availableStockQty = this.availableStockQty - qty;

        this.addHistory(new StockHistory("DECREASE", qty, orderId));
    }

    public void increase(final String orderId, final Long qty) {
        this.availableStockQty = this.availableStockQty + qty;

        this.addHistory(new StockHistory("INCREASE", qty, orderId));
    }

    private void addHistory(StockHistory history) {
        this.stockHistories.add(history);

        if(history.getStock() != this) {
            history.setStock(this);
        }
    }

    public List<StockHistory> getStockHistories() {
        return stockHistories;
    }

    @Override
    public String toString() {
        return "Stock{" +
                "id=" + id +
                ", productId='" + productId + '\'' +
                ", availableStockQty=" + availableStockQty +
                '}';
    }
}
