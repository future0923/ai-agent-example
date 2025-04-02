package io.github.future0923.ai.agent.example.flight.booking.tools;

import io.github.future0923.ai.agent.example.flight.booking.entity.FlightBooking;
import io.github.future0923.ai.agent.example.flight.booking.service.FlightBookingService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * @author future0923
 */
@Component
public class FlightBookingTools {

    private final FlightBookingService service;

    public FlightBookingTools(FlightBookingService service) {
        this.service = service;
    }

    @Tool(description = "获取用户所有的机票信息")
    public List<FlightBooking> getUserBookings(@ToolParam(description = "用户名") String username) {
        return service.getUserBookings(username);
    }

    public record BookingRecordDTO(
            @ToolParam(description = "起飞日期") LocalDate bookingTo,
            @ToolParam(description = "用户名") String name,
            @ToolParam(description = "出发地") String from,
            @ToolParam(description = "目的地") String to) {

    }

    @Tool(description = "预订机票")
    public String bookings(@ToolParam(description = "预订机票必要参数") BookingRecordDTO dto) {
        return service.bookings(dto);
    }

    @Tool(description = "机票取消预订")
    public String cancelBookings(@ToolParam(description = "预订号") String bookingNumber) {
        return service.cancelBookings(bookingNumber);
    }

    @Tool(description = "根据预定号查旬机票信息")
    public FlightBooking bookingsInfo(@ToolParam(description = "预订号") String bookingNumber) {
        return service.bookingsInfo(bookingNumber);
    }
}
