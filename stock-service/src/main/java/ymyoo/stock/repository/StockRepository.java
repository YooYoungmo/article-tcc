package ymyoo.stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ymyoo.stock.entity.Stock;

public interface StockRepository extends JpaRepository<Stock, Long> {
    Stock findByProductId(String productId);

    @Query("select s from Stock s where exists ( select sh.orderId from s.stockHistories sh where sh.orderId = ?1)")
    Stock findByOrderId(String orderId);
}
