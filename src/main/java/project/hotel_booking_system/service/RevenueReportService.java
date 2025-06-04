package project.hotel_booking_system.service;

import org.springframework.security.access.prepost.PreAuthorize;

import java.time.LocalDate;
import java.util.List;

import project.hotel_booking_system.dto.response.RevenueReportDTO;
import project.hotel_booking_system.enums.ReportPeriod;

public interface RevenueReportService {

    List<RevenueReportDTO> getDailyRevenueReport(LocalDate startDate, LocalDate endDate);

    List<RevenueReportDTO> getMonthlyRevenueReport(LocalDate startDate, LocalDate endDate);

    List<RevenueReportDTO> getYearlyRevenueReport(LocalDate startDate, LocalDate endDate);

    List<RevenueReportDTO> getRevenueReport(LocalDate startDate, LocalDate endDate, ReportPeriod period);
} 