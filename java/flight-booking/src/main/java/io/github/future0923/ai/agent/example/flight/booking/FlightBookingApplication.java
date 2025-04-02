package io.github.future0923.ai.agent.example.flight.booking;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author future0923
 */
@SpringBootApplication
@MapperScan("io.github.future0923.ai.agent.example.flight.booking.dao")
public class FlightBookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlightBookingApplication.class, args);
    }
}
