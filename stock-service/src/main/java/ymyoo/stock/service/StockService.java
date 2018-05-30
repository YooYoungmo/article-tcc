package ymyoo.stock.service;

import ymyoo.stock.dto.StockAdjustment;
import ymyoo.stock.entity.ReservedStock;

import java.time.LocalDateTime;

public interface StockService {
    ReservedStock reserveStock(StockAdjustment stockAdjustment);

    void confirmStock(Long id, LocalDateTime confirmedTime);

    void cancelStock(Long id);

    void decreaseStock(String productId, Long qty);
}
