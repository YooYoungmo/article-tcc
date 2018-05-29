package ymyoo.payment.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

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


    public static PaymentRequest deserializeJSON(final String json) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, PaymentRequest.class);
    }

    public String serializeJSON() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(this);
    }

    public void validate() {
        if(this.paymentAmt >= 300000L) {
            throw new IllegalArgumentException("결제 제한 금액 초과");
        }
    }
}
