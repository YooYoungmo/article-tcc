package ymyoo.stock.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    private static final Logger log = LoggerFactory.getLogger(StockServiceImpl.class);

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
        ReservedStock reservedStock = new ReservedStock(stockAdjustment);

        reservedStockRepository.save(reservedStock);

        log.info("Reserved Stock :" + reservedStock.getId());
        return reservedStock;
    }

    @Transactional
    @Override
    public void confirmStock(Long id) {
        ReservedStock reservedStock = reservedStockRepository.findOne(id);

        validateReservedStock(reservedStock);

        if(reservedStock.getResources().getAdjustmentType().equals("REDUCE")) {
            Stock stock = stockRepository.findByProductId(reservedStock.getResources().getProductId());
            stock.decrease(reservedStock.getResources().getQty());

            stockRepository.save(stock);
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
            // 차감된 재고 되돌리기...
            rollbackStock(reservedStock);
        }

        reservedStock.setStatus(Status.CANCEL);
        reservedStockRepository.save(reservedStock);

        log.info("Cancel Stock :" + id);
    }

    private void rollbackStock(ReservedStock reservedStock) {
        Stock stock = stockRepository.findByProductId(reservedStock.getResources().getProductId());

        if(reservedStock.getResources().getAdjustmentType().equals("REDUCE")) {
            log.info("Before adjustStock : " + stock.toString());

            stock.decrease(reservedStock.getResources().getQty());
            stockRepository.save(stock);

            log.info("After adjustStock : " + stock.toString());
        }
    }
}
