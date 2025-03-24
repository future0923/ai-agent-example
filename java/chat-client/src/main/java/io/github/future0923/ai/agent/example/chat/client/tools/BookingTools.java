package io.github.future0923.ai.agent.example.chat.client.tools;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

/**
 * @author future0923
 */
@Configuration
public class BookingTools {

    @JsonClassDescription("预订的相关信息")
    public record CancelBookingRequest(
            @JsonProperty(required = true, value = "bookingNumber") @JsonPropertyDescription("预订号, 比如1001***") String bookingNumber,
            @JsonProperty(required = true, value = "name") @JsonPropertyDescription("用户名, 比如张三、李四.....") String name) {
    }

    @Bean
    @Description("取消机票预订")
    public Function<CancelBookingRequest, String> cancelBooking() {
        return request -> {
            System.out.println(request);
            //return "对不起现在已经过了时间了";
            return "取消成功";
        };
    }
}
