package project.hotel_booking_system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import project.hotel_booking_system.dto.request.room_request.BlockDatesRequest;
import project.hotel_booking_system.dto.response.*;
import project.hotel_booking_system.model.RoomBlockedDate;
import project.hotel_booking_system.service.room.RoomAvailabilityService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoomAvailabilityController.class)
class RoomAvailabilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomAvailabilityService roomAvailabilityService;

    @Autowired
    private ObjectMapper objectMapper;

    private RoomAvailabilityResponse roomAvailabilityResponse;
    private PaginationResponse<RoomAvailabilityPageResponse> paginationResponse;
    private PaginationResponse<CalendarDayResponse> calendarResponse;
    private PaginationResponse<RoomBlockedDate> blockedDatesResponse;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.of(2025, 6, 15);
        endDate = LocalDate.of(2025, 6, 20);

        // Setup RoomAvailabilityResponse
        roomAvailabilityResponse = RoomAvailabilityResponse.builder()
                .roomId(1L)
                .roomNumber("101")
                .availableDates(List.of(startDate, startDate.plusDays(1)))
                .bookedDates(List.of())
                .blockedDates(List.of())
                .build();

        // Setup PaginationResponse for rooms
        RoomAvailabilityPageResponse pageResponse = RoomAvailabilityPageResponse.builder()
                .roomId(1L)
                .roomNumber("101")
                .build();

        paginationResponse = PaginationResponse.<RoomAvailabilityPageResponse>builder()
                .content(List.of(pageResponse))
                .page(0)
                .pageSize(10)
                .totalElements(1L)
                .totalPages(1)
                .last(true)
                .build();

        // Setup CalendarDayResponse
        RoomCalendarInfo calendarInfo = RoomCalendarInfo.builder()
                .roomId(1L)
                .roomNumber("101")
                .status("AVAILABLE")
                .bookingInfo("")
                .build();

        CalendarDayResponse dayResponse = CalendarDayResponse.builder()
                .date(startDate)
                .rooms(List.of(calendarInfo))
                .build();

        calendarResponse = PaginationResponse.<CalendarDayResponse>builder()
                .content(List.of(dayResponse))
                .page(0)
                .pageSize(7)
                .totalElements(30L)
                .totalPages(5)
                .last(false)
                .build();

        // Setup RoomBlockedDate
        RoomBlockedDate blockedDate = RoomBlockedDate.builder()
                .id(1L)
                .blockedDate(startDate)
                .reason("Maintenance")
                .createdAt(LocalDateTime.now())
                .createdBy(1L)
                .build();

        blockedDatesResponse = PaginationResponse.<RoomBlockedDate>builder()
                .content(List.of(blockedDate))
                .page(0)
                .pageSize(10)
                .totalElements(1L)
                .totalPages(1)
                .last(true)
                .build();
    }

    @Test
    @WithMockUser
    void getRoomAvailability_ValidInput_Success() throws Exception {
        // Given
        when(roomAvailabilityService.getRoomAvailability(1L, startDate, endDate))
                .thenReturn(roomAvailabilityResponse);

        // When & Then
        mockMvc.perform(get("/rooms/1/availability")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Room availability retrieved successfully"))
                .andExpect(jsonPath("$.result.roomId").value(1))
                .andExpect(jsonPath("$.result.roomNumber").value("101"));
    }

    @Test
    @WithMockUser
    void getRoomAvailability_MissingParameters_BadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/rooms/1/availability")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getAllRoomsAvailability_ValidInput_Success() throws Exception {
        // Given
        when(roomAvailabilityService.getAllRoomsAvailability(eq(startDate), eq(endDate), any(Pageable.class)))
                .thenReturn(paginationResponse);

        // When & Then
        mockMvc.perform(get("/rooms/availability")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "roomNumber")
                        .param("sortDir", "asc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("All rooms availability retrieved successfully"))
                .andExpect(jsonPath("$.result.content").isArray())
                .andExpect(jsonPath("$.result.page").value(0))
                .andExpect(jsonPath("$.result.pageSize").value(10));
    }

    @Test
    @WithMockUser
    void getAllRoomsAvailability_WithDefaultParameters_Success() throws Exception {
        // Given
        when(roomAvailabilityService.getAllRoomsAvailability(eq(startDate), eq(endDate), any(Pageable.class)))
                .thenReturn(paginationResponse);

        // When & Then
        mockMvc.perform(get("/rooms/availability")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.page").value(0))
                .andExpect(jsonPath("$.result.pageSize").value(10));
    }

    @Test
    @WithMockUser
    void getCalendarViewPaginated_ValidInput_Success() throws Exception {
        // Given - SỬA ĐÂY: dùng eq() cho tất cả parameters
        when(roomAvailabilityService.getCalendarViewPaginated(eq(2025), eq(6), any(Pageable.class)))
                .thenReturn(calendarResponse);

        // When & Then
        mockMvc.perform(get("/rooms/availability/calendar")
                        .param("year", "2025")
                        .param("month", "6")
                        .param("page", "0")
                        .param("size", "7")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Paginated calendar view retrieved successfully"))
                .andExpect(jsonPath("$.result.content").isArray())
                .andExpect(jsonPath("$.result.page").value(0))
                .andExpect(jsonPath("$.result.pageSize").value(7));
    }

    @Test
    @WithMockUser
    void getCalendarViewPaginated_InvalidMonth_BadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/rooms/availability/calendar")
                        .param("year", "2025")
                        .param("month", "13") // Invalid month
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk()) // Controller returns OK with error message
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid month. Month must be between 1 and 12"));
    }

    @Test
    @WithMockUser
    void getCalendarViewPaginated_MonthZero_BadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/rooms/availability/calendar")
                        .param("year", "2025")
                        .param("month", "0") // Invalid month
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk()) // Controller returns OK with error message
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid month. Month must be between 1 and 12"));
    }

    @Test
    @WithMockUser
    void getBlockedDates_ValidInput_Success() throws Exception {
        // Given
        when(roomAvailabilityService.getBlockedDates(eq(1L), eq(startDate), eq(endDate), any(Pageable.class)))
                .thenReturn(blockedDatesResponse);

        // When & Then
        mockMvc.perform(get("/rooms/1/blocked-dates")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Blocked dates retrieved successfully"))
                .andExpect(jsonPath("$.result.content").isArray())
                .andExpect(jsonPath("$.result.page").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void blockDates_ValidInput_Success() throws Exception {
        // Given
        BlockDatesRequest request = BlockDatesRequest.builder()
                .blockDates(List.of(startDate, startDate.plusDays(1)))
                .reason("Maintenance")
                .build();

        // When & Then
        mockMvc.perform(put("/rooms/1/block-dates")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Dates blocked successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void blockDates_InvalidRequest_BadRequest() throws Exception {
        BlockDatesRequest request = BlockDatesRequest.builder()
                .blockDates(List.of())
                .reason("Maintenance")
                .build();


        // When & Then
        mockMvc.perform(put("/rooms/1/block-dates")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void unblockDates_ValidInput_Success() throws Exception {
        // When & Then
        mockMvc.perform(delete("/rooms/1/block-dates")
                        .with(csrf())
                        .param("dates", startDate.toString(), startDate.plusDays(1).toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Dates unblocked successfully"));
    }

    @Test
    @WithMockUser
    void quickAvailabilityCheck_Available_Success() throws Exception {
        // Given
        LocalDate checkIn = startDate;
        LocalDate checkOut = startDate.plusDays(2);

        RoomAvailabilityResponse availabilityResponse = RoomAvailabilityResponse.builder()
                .roomId(1L)
                .roomNumber("101")
                .availableDates(List.of(checkIn, checkIn.plusDays(1))) // Both dates available
                .bookedDates(List.of())
                .blockedDates(List.of())
                .build();

        when(roomAvailabilityService.getRoomAvailability(1L, checkIn, checkOut.minusDays(1)))
                .thenReturn(availabilityResponse);

        // When & Then
        mockMvc.perform(get("/rooms/1/availability/quick")
                        .param("checkIn", checkIn.toString())
                        .param("checkOut", checkOut.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Quick availability check completed"))
                .andExpect(jsonPath("$.result").value(true));
    }

    @Test
    @WithMockUser
    void quickAvailabilityCheck_NotAvailable_Success() throws Exception {
        // Given
        LocalDate checkIn = startDate;
        LocalDate checkOut = startDate.plusDays(2);

        RoomAvailabilityResponse availabilityResponse = RoomAvailabilityResponse.builder()
                .roomId(1L)
                .roomNumber("101")
                .availableDates(List.of(checkIn)) // Only first date available
                .bookedDates(List.of(checkIn.plusDays(1)))
                .blockedDates(List.of())
                .build();

        when(roomAvailabilityService.getRoomAvailability(1L, checkIn, checkOut.minusDays(1)))
                .thenReturn(availabilityResponse);

        // When & Then
        mockMvc.perform(get("/rooms/1/availability/quick")
                        .param("checkIn", checkIn.toString())
                        .param("checkOut", checkOut.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Quick availability check completed"))
                .andExpect(jsonPath("$.result").value(false));
    }

    @Test
    @WithMockUser
    void quickAvailabilityCheck_MissingParameters_BadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/rooms/1/availability/quick")
                        .param("checkIn", startDate.toString())
                        // Missing checkOut parameter
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllEndpoints_WithoutAuthentication_Unauthorized() throws Exception {
        // Test GET endpoints without authentication
        mockMvc.perform(get("/rooms/1/availability")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/rooms/availability")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/rooms/availability/calendar")
                        .param("year", "2025")
                        .param("month", "6"))
                .andExpect(status().isUnauthorized());
    }

}