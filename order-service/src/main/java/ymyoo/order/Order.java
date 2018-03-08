package ymyoo.order;

public class Order {
    private String orderId;
    private String productId;
    private Long qty;
    private Long paymentAmt;

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setPaymentAmt(Long paymentAmt) {
        this.paymentAmt = paymentAmt;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getProductId() {
        return productId;
    }

    public Long getPaymentAmt() {
        return paymentAmt;
    }

    public Long getQty() {
        return qty;
    }

    public void setQty(Long qty) {
        this.qty = qty;
    }
}
