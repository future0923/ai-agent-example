package io.github.future0923.ai.agent.example.function.calling.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日期时间工具
 *
 * @author future0923
 */
public class DateTimeTools {

    /**
     * 获取当前时间工具
     */
    @Tool(description = "获取用户所在时区的当前日期和时间。")
    public String getCurrentTime() {
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
    }

    /**
     * 设置日期提醒工具
     */
    @Tool(description = "按照ISO-8601格式设置给定时间的用户提醒。")
    public void setAlarm(@ToolParam(required = true, description = "以ISO-8601格式的时间") String time) {
        LocalDateTime alarmTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
        System.out.println("Alarm set for " + alarmTime);
    }
}
