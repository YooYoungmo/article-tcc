package ymyoo.stock.adapter.messaging.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import ymyoo.stock.adapter.messaging.StockAdjustmentChannelAdapter;
import ymyoo.stock.dto.StockAdjustment;
import ymyoo.stock.repository.ReservedStockRepository;
import ymyoo.stock.service.StockService;

@Component
public class StockAdjustmentChannelAdapterKafkaImpl implements StockAdjustmentChannelAdapter {
    private static final Logger log = LoggerFactory.getLogger(StockAdjustmentChannelAdapterKafkaImpl.class);

    private final String TOPIC = "stock-adjustment";

    private ReservedStockRepository reservedStockRepository;

    private StockService stockService;

    @Autowired
    private KafkaTemplate<Long, String> kafkaTemplate;

    @Autowired
    public void setReservedStockRepository(ReservedStockRepository reservedStockRepository) {
        this.reservedStockRepository = reservedStockRepository;
    }

    @Autowired
    public void setStockService(StockService stockService) {
        this.stockService = stockService;
    }

    @Override
    public void publish(final String message) {
        this.kafkaTemplate.send(TOPIC, message);
    }

    @KafkaListener(topics = TOPIC)
    public void subscribe(final String message, Acknowledgment ack) {
        log.info(String.format("Message Received : [%s]", message));
        try {
            StockAdjustment stockAdjustment = StockAdjustment.deserializeJSON(message);

            // 이미 처리된 주문인지 확인
            if(stockService.isAlreadyProcessedOrderId(stockAdjustment.getOrderId())) {
                log.info(String.format("AlreadyProcessedOrderId : [%s]", stockAdjustment.getOrderId()));
            } else {
                // 미 처리 주문일 경우 재고 처리
                if(stockAdjustment.getAdjustmentType().equals("REDUCE")) {
                    stockService.decreaseStock(stockAdjustment);
                }
            }
            // Kafka Offset Manual Commit
            ack.acknowledge();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}