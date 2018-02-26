package ymyoo.order;

public class Order {
    private String orderId;
    private String productId;
    private Integer qty;
    private Long paymentAmt;

    public Order(String productId, Integer qty, Long paymentAmt) {
        this.orderId = generateOrderId();
        this.productId = productId;
        this.paymentAmt = paymentAmt;
        this.qty = qty;
    }

    private String generateOrderId() {
        return java.util.UUID.randomUUID().toString().toUpperCase();
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

    public Integer getQty() {
        return qty;
    }
}
