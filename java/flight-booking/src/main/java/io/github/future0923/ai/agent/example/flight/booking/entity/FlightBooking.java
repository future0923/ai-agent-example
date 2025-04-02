package io.github.future0923.ai.agent.example.flight.booking.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.future0923.ai.agent.example.flight.booking.enums.BookingClass;
import io.github.future0923.ai.agent.example.flight.booking.enums.BookingStatus;
import org.springframework.ai.tool.annotation.ToolParam;

import java.time.LocalDate;

@TableName("flight_booking")
public class FlightBooking {

    @ToolParam(description = "预定号")
    @TableId
    private String bookingNumber;
    @ToolParam(description = "预定日期")
    private LocalDate date;
    @ToolParam(description = "起飞日期")
    private LocalDate bookingTo;
    @ToolParam(description = "用户名")
    private String name;
    @ToolParam(description = "出发地")
    @TableField("`from`")
    private String from;
    @ToolParam(description = "目的地")
    @TableField("`to`")
    private String to;
    @ToolParam(description = "状态，取值为 CONFIRMED已确认, COMPLETED已完成, CANCELLED已取消")
    private BookingStatus bookingStatus;
    @ToolParam(description = "机票类型，ECONOMY经济舱, PREMIUM_ECONOMY豪华经济舱, BUSINESS头等舱")
    private BookingClass bookingClass;

    public String getBookingNumber() {
        return bookingNumber;
    }

    public void setBookingNumber(String bookingNumber) {
        this.bookingNumber = bookingNumber;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalDate getBookingTo() {
        return bookingTo;
    }

    public void setBookingTo(LocalDate bookingTo) {
        this.bookingTo = bookingTo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public BookingStatus getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(BookingStatus bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public BookingClass getBookingClass() {
        return bookingClass;
    }

    public void setBookingClass(BookingClass bookingClass) {
        this.bookingClass = bookingClass;
    }
}