package ymyoo.payment.dto;

public class PaymentRequest {
    private String orderId;
    private Long paymentAmt;


    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Long getPaymentAmt() {
        return paymentAmt;
    }

    public void setPaymentAmt(Long paymentAmt) {
        this.paymentAmt = paymentAmt;
    }
}
