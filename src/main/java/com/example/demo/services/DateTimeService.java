package com.example.demo.services;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class DateTimeService {

    // (00:00:00 day 1)
    public LocalDateTime getStartOfMonth() {
        return LocalDate.now()
                .withDayOfMonth(1)
                .atStartOfDay();
    }

    // last month (23:59:59.999)
    public LocalDateTime getEndOfMonth() {
        return LocalDate.now()
                .withDayOfMonth(LocalDate.now().lengthOfMonth())
                .atTime(LocalTime.MAX);
    }

    public LocalDateTime getStartOfNextMonth() {
        return LocalDate.now()
                .plusMonths(1)
                .withDayOfMonth(1)
                .atStartOfDay();
    }
}