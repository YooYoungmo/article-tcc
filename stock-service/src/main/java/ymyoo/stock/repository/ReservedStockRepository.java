package ymyoo.stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ymyoo.stock.entity.ReservedStock;

public interface ReservedStockRepository extends JpaRepository<ReservedStock, Long> {
}
