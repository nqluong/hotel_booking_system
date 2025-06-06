package project.hotel_booking_system.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.hotel_booking_system.dto.response.ApiResponseDTO;
import project.hotel_booking_system.dto.response.RevenueReportDTO;
import project.hotel_booking_system.enums.ReportPeriod;
import project.hotel_booking_system.service.common.RevenueReportService;

@RestController
@RequestMapping("/admin/reports/revenue")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Revenue Reports", description = "API endpoints for generating revenue reports")
public class RevenueController {

    private final RevenueReportService revenueReportService;
    private final JdbcTemplate jdbcTemplate;


    @GetMapping("/check")
    @Operation(
            summary = "Check data availability",
            description = "Check if payment data is available for reporting",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    public ApiResponseDTO<String> checkDataAvailability() {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM payments", Integer.class);

            return ApiResponseDTO.<String>builder()
                    .status(HttpStatus.OK.value())
                    .time(LocalDateTime.now())
                    .success(true)
                    .message("Payment data check completed")
                    .result("Found " + count + " payment records in database")
                    .build();
        } catch (Exception e) {
            log.error("Error checking payment data: {}", e.getMessage(), e);
            return ApiResponseDTO.<String>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .time(LocalDateTime.now())
                    .success(false)
                    .message("Error checking payment data: " + e.getMessage())
                    .build();
        }
    }

    @GetMapping("/daily")
    @Operation(
            summary = "Get daily revenue report",
            description = "Generate revenue report grouped by day within the given date range",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully generated report"),
            @ApiResponse(responseCode = "400", description = "Invalid date range",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    public ApiResponseDTO<List<RevenueReportDTO>> getDailyReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            validateDateRange(startDate, endDate);
            List<RevenueReportDTO> reportData = revenueReportService.getDailyRevenueReport(startDate, endDate);

            return ApiResponseDTO.<List<RevenueReportDTO>>builder()
                    .status(HttpStatus.OK.value())
                    .time(LocalDateTime.now())
                    .success(true)
                    .message("Daily revenue report generated successfully")
                    .result(reportData)
                    .build();
        } catch (Exception e) {
            log.error("Error generating daily report: {}", e.getMessage(), e);
            return ApiResponseDTO.<List<RevenueReportDTO>>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .time(LocalDateTime.now())
                    .success(false)
                    .message("Error generating report: " + e.getMessage())
                    .result(Collections.emptyList())
                    .build();
        }
    }

    @GetMapping("/monthly")
    @Operation(
            summary = "Get monthly revenue report",
            description = "Generate revenue report grouped by month within the given date range",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully generated report"),
            @ApiResponse(responseCode = "400", description = "Invalid date range",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    public ApiResponseDTO<List<RevenueReportDTO>> getMonthlyReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("Generating monthly revenue report from {} to {}", startDate, endDate);

        try {
            validateDateRange(startDate, endDate);
            List<RevenueReportDTO> reportData = revenueReportService.getMonthlyRevenueReport(startDate, endDate);

            return ApiResponseDTO.<List<RevenueReportDTO>>builder()
                    .status(HttpStatus.OK.value())
                    .time(LocalDateTime.now())
                    .success(true)
                    .message("Monthly revenue report generated successfully")
                    .result(reportData)
                    .build();
        } catch (Exception e) {
            log.error("Error generating monthly report: {}", e.getMessage(), e);
            return ApiResponseDTO.<List<RevenueReportDTO>>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .time(LocalDateTime.now())
                    .success(false)
                    .message("Error generating report: " + e.getMessage())
                    .result(Collections.emptyList())
                    .build();
        }
    }

    @GetMapping("/yearly")
    @Operation(
            summary = "Get yearly revenue report",
            description = "Generate revenue report grouped by year within the given date range",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully generated report"),
            @ApiResponse(responseCode = "400", description = "Invalid date range",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    public ApiResponseDTO<List<RevenueReportDTO>> getYearlyReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("Generating yearly revenue report from {} to {}", startDate, endDate);

        try {
            validateDateRange(startDate, endDate);
            List<RevenueReportDTO> reportData = revenueReportService.getYearlyRevenueReport(startDate, endDate);

            return ApiResponseDTO.<List<RevenueReportDTO>>builder()
                    .status(HttpStatus.OK.value())
                    .time(LocalDateTime.now())
                    .success(true)
                    .message("Yearly revenue report generated successfully")
                    .result(reportData)
                    .build();
        } catch (Exception e) {
            log.error("Error generating yearly report: {}", e.getMessage(), e);
            return ApiResponseDTO.<List<RevenueReportDTO>>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .time(LocalDateTime.now())
                    .success(false)
                    .message("Error generating report: " + e.getMessage())
                    .result(Collections.emptyList())
                    .build();
        }
    }

    @GetMapping
    @Operation(
            summary = "Get revenue report by period type",
            description = "Generate revenue report with custom period type (DAILY, MONTHLY, YEARLY)",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully generated report"),
            @ApiResponse(responseCode = "400", description = "Invalid date range or period type",
                    content = @Content(schema = @Schema(implementation = ApiResponseDTO.class)))
    })
    public ApiResponseDTO<List<RevenueReportDTO>> getReportByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam ReportPeriod period) {


        try {
            validateDateRange(startDate, endDate);
            List<RevenueReportDTO> reportData = revenueReportService.getRevenueReport(startDate, endDate, period);

            return ApiResponseDTO.<List<RevenueReportDTO>>builder()
                    .status(HttpStatus.OK.value())
                    .time(LocalDateTime.now())
                    .success(true)
                    .message(period + " revenue report generated successfully")
                    .result(reportData)
                    .build();
        } catch (Exception e) {
            log.error("Error generating {} report: {}", period, e.getMessage(), e);
            return ApiResponseDTO.<List<RevenueReportDTO>>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .time(LocalDateTime.now())
                    .success(false)
                    .message("Error generating report: " + e.getMessage())
                    .result(Collections.emptyList())
                    .build();
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date must be provided");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }

        if (startDate.plusYears(2).isBefore(endDate)) {
            throw new IllegalArgumentException("Date range cannot exceed 2 years");
        }
    }

} 