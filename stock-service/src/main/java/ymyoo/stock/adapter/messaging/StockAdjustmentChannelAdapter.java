package ymyoo.stock.adapter.messaging;

public interface StockAdjustmentChannelAdapter {
    void publish(String message);
}
