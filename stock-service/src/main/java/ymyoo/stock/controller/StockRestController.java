package ymyoo.stock.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ymyoo.stock.dto.ParticipantLink;
import ymyoo.stock.dto.StockAdjustment;
import ymyoo.stock.entity.ReservedStock;
import ymyoo.stock.service.StockService;

import java.net.URI;
import java.util.Date;

@RestController
@RequestMapping("/api/v1/stocks")
public class StockRestController {
    private StockService stockService;

    @Autowired
    public void setStockService(StockService stockService) {
        this.stockService = stockService;
    }

    @PostMapping
    public ResponseEntity<ParticipantLink> tryStockAdjustment(@RequestBody StockAdjustment stockAdjustment) {
        final ReservedStock reservedStock = stockService.reserveStock(stockAdjustment);

        final ParticipantLink participantLink = buildParticipantLink(reservedStock.getId(), reservedStock.getCreated(), reservedStock.getTimeout());

        return new ResponseEntity<>(participantLink, HttpStatus.CREATED);
    }

    private ParticipantLink buildParticipantLink(final Long id, final Date created, final Long timeout) {
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
        final long expires = created.getTime() + timeout;

        return new ParticipantLink(location, new Date(expires));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> confirmStockAdjustment(@PathVariable Long id) {
        try {
            stockService.confirmStock(id);
        } catch(IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelStockAdjustment(@PathVariable Long id) {
        stockService.cancelStock(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
