package ymyoo.payment.adapter.messaging;

public interface PaymentOrderChannelAdapter {
    void publish(String message);
}
