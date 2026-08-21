package be.lennertsoffers.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

@SpringBootApplication(scanBasePackages = "be.lennertsoffers")
public class StoreServiceApplication {

    static void main(String[] args) {
        SpringApplication.run(StoreServiceApplication.class, args);
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

}
