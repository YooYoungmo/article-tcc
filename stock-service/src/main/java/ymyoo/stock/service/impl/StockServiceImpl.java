package ymyoo.stock.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ymyoo.stock.AdjustmentType;
import ymyoo.stock.Status;
import ymyoo.stock.controller.StockRestController;
import ymyoo.stock.dto.StockAdjustment;
import ymyoo.stock.entity.ReservedStock;
import ymyoo.stock.entity.Stock;
import ymyoo.stock.repository.ReservedStockRepository;
import ymyoo.stock.repository.StockRepository;
import ymyoo.stock.service.StockService;

import java.util.concurrent.TimeUnit;

@Service
public class StockServiceImpl implements StockService {
    // 3초 타임 아웃
    public static final long TIMEOUT = TimeUnit.SECONDS.toMillis(3);

    private static final Logger log = LoggerFactory.getLogger(StockRestController.class);

    private ReservedStockRepository reservedStockRepository;
    private StockRepository stockRepository;

    @Autowired
    public void setReservedStockRepository(ReservedStockRepository reservedStockRepository) {
        this.reservedStockRepository = reservedStockRepository;
    }

    @Autowired
    public void setStockRepository(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Override
    public ReservedStock reserveStock(final StockAdjustment stockAdjustment) {
        ReservedStock reservedStock = new ReservedStock(AdjustmentType.valueOf(stockAdjustment.getAdjustmentType()),
                stockAdjustment.getProductId(),
                stockAdjustment.getQty());

        reservedStockRepository.save(reservedStock);

        log.info("Reserved Stock :" + reservedStock.getId());
        return reservedStock;
    }

    @Transactional
    @Override
    public void confirmStock(Long id) {
        ReservedStock reservedStock = reservedStockRepository.findOne(id);

        validateReservedStock(reservedStock);

        if(reservedStock.getAdjustmentType() == AdjustmentType.REDUCE) {
            Stock stock = stockRepository.findByProductId(reservedStock.getProductId());
            log.info("Before adjustStock : " + stock.toString());

            stock.reduce(reservedStock.getQty());
            stockRepository.save(stock);

            log.info("After adjustStock : " + stock.toString());
        }

        reservedStock.setStatus(Status.CONFIRMED);
        reservedStockRepository.save(reservedStock);

        log.info("Confirm Stock :" + id);
    }

    private void validateReservedStock(ReservedStock reservedStock) {
        validateStatus(reservedStock);
        validateExpired(reservedStock);
    }

    private void validateExpired(ReservedStock reservedStock) {
        final long confirmTime = System.currentTimeMillis();
        final long reservedTime = reservedStock.getCreated().getTime();

        final long duration = confirmTime - reservedTime;

        if(duration > TIMEOUT) {
            throw new IllegalArgumentException("Expired");
        }
    }

    private void validateStatus(ReservedStock reservedStock) {
        if(reservedStock.getStatus() == Status.CANCEL) {
            throw new IllegalArgumentException("Invalidate Status");
        }
    }

    @Transactional
    @Override
    public void cancelStock(Long id) {
        ReservedStock reservedStock = reservedStockRepository.findOne(id);

        if(reservedStock.getStatus() == Status.CONFIRMED) {
            // 이미 Confirm 되었다면..
            // 재고 되돌리기... 로직
        }

        reservedStock.setStatus(Status.CANCEL);
        reservedStockRepository.save(reservedStock);

        log.info("Cancel Stock :" + id);
    }
}
