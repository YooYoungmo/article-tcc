package ymyoo.stock.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ymyoo.stock.dto.StockAdjustment;
import ymyoo.stock.entity.Stock;
import ymyoo.stock.repository.StockRepository;

@RestController
@RequestMapping("/api/v1/products")
public class StockController {
    private static final Logger log = LoggerFactory.getLogger(StockController.class);

    private StockRepository stockRepository;

    @Autowired
    public void setStockRepository(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @PutMapping("/{productId}")
    public ResponseEntity<Void> adjustStock(@PathVariable String productId,
                                            @RequestBody StockAdjustment stockAdjustment) {
        if(stockAdjustment.getAdjustmentType().equals("REDUCE")) {
            Stock findStock = stockRepository.findByProductId(productId);
            log.info("Before adjustStock : " + findStock.toString());

            findStock.reduce(stockAdjustment.getQty());
            stockRepository.save(findStock);

            log.info("After adjustStock : " + findStock.toString());
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
