package ymyoo.stock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import ymyoo.stock.entity.Stock;
import ymyoo.stock.repository.StockRepository;

import java.util.List;

@SpringBootApplication
public class StockApplication {
    private static final Logger log = LoggerFactory.getLogger(StockApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(StockApplication.class);
    }

    @Bean
    public CommandLineRunner initData(StockRepository repository) {
        return (args) -> {
            // 재고 데이터 삽입
            repository.save(new Stock("prd-0001", 10l));
            repository.save(new Stock("prd-0002", 20l));
            repository.save(new Stock("prd-0003", 30l));

            repository.flush();


            List<Stock> findAll = repository.findAll();
            findAll.forEach(stock -> log.info(stock.toString()));
        };
    }


}
