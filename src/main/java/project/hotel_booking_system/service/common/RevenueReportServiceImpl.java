package project.hotel_booking_system.service.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.hotel_booking_system.dto.response.RevenueReportDTO;
import project.hotel_booking_system.enums.BookingStatus;
import project.hotel_booking_system.enums.PaymentStatus;
import project.hotel_booking_system.enums.ReportPeriod;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RevenueReportServiceImpl implements RevenueReportService {

    private final EntityManager entityManager;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<RevenueReportDTO> getDailyRevenueReport(LocalDate startDate, LocalDate endDate) {
        return getRevenueReport(startDate, endDate, ReportPeriod.DAILY);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<RevenueReportDTO> getMonthlyRevenueReport(LocalDate startDate, LocalDate endDate) {
        return getRevenueReport(startDate, endDate, ReportPeriod.MONTHLY);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<RevenueReportDTO> getYearlyRevenueReport(LocalDate startDate, LocalDate endDate) {
        return getRevenueReport(startDate, endDate, ReportPeriod.YEARLY);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<RevenueReportDTO> getRevenueReport(LocalDate startDate, LocalDate endDate, ReportPeriod period) {
        try {

            String sqlQuery;
            
            switch (period) {
                case DAILY:
                    sqlQuery = "SELECT DATE(p.payment_date) as report_date, " +
                            "SUM(p.amount) as total_revenue, " +
                            "COUNT(p.id) as bookings_count " +
                            "FROM payments p " +
                            "JOIN bookings b ON p.booking_id = b.id " +
                            "WHERE p.status = :paymentStatus " +
                            "AND b.status = :bookingStatus " +
                            "AND DATE(p.payment_date) >= :startDate " +
                            "AND DATE(p.payment_date) <= :endDate " +
                            "GROUP BY DATE(p.payment_date) " +
                            "ORDER BY report_date";
                    break;
                case MONTHLY:
                    sqlQuery = "SELECT DATE_FORMAT(p.payment_date, '%Y-%m-01') as report_date, " +
                            "SUM(p.amount) as total_revenue, " +
                            "COUNT(p.id) as bookings_count " +
                            "FROM payments p " +
                            "JOIN bookings b ON p.booking_id = b.id " +
                            "WHERE p.status = :paymentStatus " +
                            "AND b.status = :bookingStatus " +
                            "AND DATE(p.payment_date) >= :startDate " +
                            "AND DATE(p.payment_date) <= :endDate " +
                            "GROUP BY DATE_FORMAT(p.payment_date, '%Y-%m-01') " +
                            "ORDER BY report_date";
                    break;
                case YEARLY:
                    sqlQuery = "SELECT DATE_FORMAT(p.payment_date, '%Y-01-01') as report_date, " +
                            "SUM(p.amount) as total_revenue, " +
                            "COUNT(p.id) as bookings_count " +
                            "FROM payments p " +
                            "JOIN bookings b ON p.booking_id = b.id " +
                            "WHERE p.status = :paymentStatus " +
                            "AND b.status = :bookingStatus " +
                            "AND DATE(p.payment_date) >= :startDate " +
                            "AND DATE(p.payment_date) <= :endDate " +
                            "GROUP BY DATE_FORMAT(p.payment_date, '%Y-01-01') " +
                            "ORDER BY report_date";
                    break;
                default:
                    throw new IllegalArgumentException("Invalid report period: " + period);
            }
            

            Query query = entityManager.createNativeQuery(sqlQuery);
            query.setParameter("paymentStatus", PaymentStatus.COMPLETED.toString());
            query.setParameter("bookingStatus", BookingStatus.COMPLETED.toString());
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            
            log.info("Executing revenue report query for period {}: {}", period, sqlQuery);
            log.info("Parameters: startDate={}, endDate={}, paymentStatus={}, bookingStatus={}", 
                    startDate, endDate, PaymentStatus.COMPLETED, BookingStatus.COMPLETED);
            

            query.setHint("javax.persistence.query.timeout", 30000);
            
            List<Object[]> results = query.getResultList();
            List<RevenueReportDTO> reportList = new ArrayList<>();
            
            log.info("Query returned {} results", results.size());
            
            for (Object[] result : results) {
                try {
                    LocalDate date = null;
                    if (result[0] instanceof java.sql.Date) {
                        date = ((java.sql.Date) result[0]).toLocalDate();
                    } else if (result[0] instanceof String) {
                        date = LocalDate.parse((String) result[0]);
                    } else {
                        log.warn("Unexpected date type: {}", result[0] != null ? result[0].getClass().getName() : "null");
                        continue;
                    }
                    
                    BigDecimal totalRevenue;
                    if (result[1] instanceof BigDecimal) {
                        totalRevenue = (BigDecimal) result[1];
                    } else if (result[1] instanceof Number) {
                        totalRevenue = BigDecimal.valueOf(((Number) result[1]).doubleValue());
                    } else {
                        log.warn("Unexpected revenue type: {}", result[1] != null ? result[1].getClass().getName() : "null");
                        totalRevenue = BigDecimal.ZERO;
                    }
                    
                    Integer bookingsCount;
                    if (result[2] instanceof Number) {
                        bookingsCount = ((Number) result[2]).intValue();
                    } else {
                        log.warn("Unexpected count type: {}", result[2] != null ? result[2].getClass().getName() : "null");
                        bookingsCount = 0;
                    }
                    
                    BigDecimal averageRevenue = BigDecimal.ZERO;
                    if (bookingsCount > 0) {
                        averageRevenue = totalRevenue.divide(BigDecimal.valueOf(bookingsCount), 2, RoundingMode.HALF_UP);
                    }
                    
                    RevenueReportDTO report = RevenueReportDTO.builder()
                            .date(date)
                            .totalRevenue(totalRevenue)
                            .bookingsCount(bookingsCount)
                            .averageRevenue(averageRevenue)
                            .build();
                    
                    reportList.add(report);
                } catch (Exception e) {
                    log.error("Error processing result row: {}", e.getMessage(), e);
                }
            }
            
            return reportList;
        } catch (Exception e) {
            log.error("Error generating revenue report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate revenue report", e);
        }
    }
} 