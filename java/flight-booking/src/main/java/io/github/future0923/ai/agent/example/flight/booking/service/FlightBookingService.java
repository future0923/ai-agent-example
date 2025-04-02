package io.github.future0923.ai.agent.example.flight.booking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.future0923.ai.agent.example.flight.booking.dao.FlightBookingDao;
import io.github.future0923.ai.agent.example.flight.booking.entity.FlightBooking;
import io.github.future0923.ai.agent.example.flight.booking.enums.BookingClass;
import io.github.future0923.ai.agent.example.flight.booking.enums.BookingStatus;
import io.github.future0923.ai.agent.example.flight.booking.tools.FlightBookingTools;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

/**
 * @author future0923
 */
@Service
public class FlightBookingService {

    private final FlightBookingDao dao;

    public FlightBookingService(FlightBookingDao dao) {
        this.dao = dao;
    }

    public List<FlightBooking> getUserBookings(String username) {
        LambdaQueryWrapper<FlightBooking> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FlightBooking::getName, username);
        return dao.selectList(queryWrapper);
    }

    public String bookings(FlightBookingTools.BookingRecordDTO dto) {
        LambdaQueryWrapper<FlightBooking> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FlightBooking::getName, dto.name());
        queryWrapper.eq(FlightBooking::getFrom, dto.from());
        queryWrapper.eq(FlightBooking::getTo, dto.to());
        if (dao.exists(queryWrapper)) {
            return "对不起你已经预订过了";
        }
        FlightBooking booking = new FlightBooking();
        booking.setBookingNumber(new Random().nextInt(1000000) + "");
        booking.setDate(LocalDate.now());
        booking.setBookingTo(dto.bookingTo());
        booking.setName(dto.name());
        booking.setFrom(dto.from());
        booking.setTo(dto.to());
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        booking.setBookingClass(BookingClass.ECONOMY);
        dao.insert(booking);
        return "预订成功";
    }

    public String cancelBookings(String bookingNumber) {
        LambdaQueryWrapper<FlightBooking> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FlightBooking::getBookingNumber, bookingNumber);
        List<FlightBooking> flightBookings = dao.selectList(queryWrapper);
        if (flightBookings.isEmpty()) {
            return "预定号不存在";
        }
        if (flightBookings.get(0).getBookingStatus() == BookingStatus.CANCELLED) {
            return "预定已经取消";
        }
        flightBookings.get(0).setBookingStatus(BookingStatus.CANCELLED);
        dao.updateById((flightBookings.get(0)));
        return "取消预订成功";
    }

    public FlightBooking bookingsInfo(String bookingNumber) {
        return dao.selectById(bookingNumber);
    }
}
