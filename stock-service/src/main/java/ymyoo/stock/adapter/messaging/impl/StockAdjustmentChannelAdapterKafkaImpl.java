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
import ymyoo.stock.service.StockService;

@Component
public class StockAdjustmentChannelAdapterKafkaImpl implements StockAdjustmentChannelAdapter {
    private static final Logger log = LoggerFactory.getLogger(StockAdjustmentChannelAdapterKafkaImpl.class);

    private final String TOPIC = "stock-adjustment";

    private StockService stockService;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public void setStockService(StockService stockService) {
        this.stockService = stockService;
    }

    @Override
    public void publish(String message) {
        this.kafkaTemplate.send(TOPIC, message);
    }

    @KafkaListener(topics = TOPIC)
    public void subscribe(String message, Acknowledgment ack) {
        log.info(String.format("Message Received : %s", message));

        try {
            StockAdjustment stockAdjustment = StockAdjustment.deserializeJSON(message);
            if(stockAdjustment.getAdjustmentType().equals("REDUCE")) {
                stockService.decreaseStock(stockAdjustment.getProductId(), stockAdjustment.getQty());
            }

            // Kafka Offset Manual Commit
            ack.acknowledge();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}