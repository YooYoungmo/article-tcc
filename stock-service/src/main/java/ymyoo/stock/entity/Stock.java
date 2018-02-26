package ymyoo.stock.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Stock {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    private String productId;

    private Long availableStockQty;

    public Stock() {
    }

    public Stock(String productId, Long availableStockQty) {
        this.productId = productId;
        this.availableStockQty = availableStockQty;
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

    public void reduce(Long qty) {
        this.availableStockQty = this.availableStockQty - qty;
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
