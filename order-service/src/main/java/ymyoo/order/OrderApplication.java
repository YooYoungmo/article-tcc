package ymyoo.order;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import ymyoo.order.service.OrderService;

@SpringBootApplication
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class);
    }

    @Bean
    public CommandLineRunner commandLineRunner(OrderService orderService) {
        return (args) -> {
            Order order = new Order("prd-0001", 10, 23000L);
            orderService.placeOrder(order);
        };
    }
}
