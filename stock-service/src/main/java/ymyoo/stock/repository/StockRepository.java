package ymyoo.stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ymyoo.stock.entity.Stock;

public interface StockRepository extends JpaRepository<Stock, Long> {
    Stock findByProductId(String productId);
}
