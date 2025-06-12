package project.hotel_booking_system.service.room;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import project.hotel_booking_system.dto.request.room_request.BlockDatesRequest;
import project.hotel_booking_system.dto.response.*;
import project.hotel_booking_system.exception.AppException;
import project.hotel_booking_system.exception.ErrorCode;
import project.hotel_booking_system.mapper.RoomAvailabilityMapper;
import project.hotel_booking_system.model.*;
import project.hotel_booking_system.repository.*;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomAvailabilityServiceImplTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RoomBlockedDateRepository blockedDateRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoomAvailabilityMapper mapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private RoomAvailabilityServiceImpl roomAvailabilityService;

    private Room room;
    private User user;
    private Booking booking;
    private RoomBlockedDate blockedDate;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.of(2025, 6, 15);
        endDate = LocalDate.of(2025, 6, 20);

        room = Room.builder()
                .id(1L)
                .roomNumber("101")
                .build();

        user = User.builder()
                .id(1L)
                .username("admin")
                .fullname("Admin User")
                .build();

        booking = Booking.builder()
                .id(1L)
                .room(room)
                .user(user)
                .checkInDate(java.util.Date.from(startDate.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant()))
                .checkOutDate(java.util.Date.from(endDate.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant()))
                .build();

        blockedDate = RoomBlockedDate.builder()
                .id(1L)
                .room(room)
                .blockedDate(startDate)
                .reason("Maintenance")
                .createdAt(LocalDateTime.now())
                .createdBy(1L)
                .build();
    }

    @Test
    void getRoomAvailability_ValidInput_Success() {
        // Given
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(bookingRepository.findBookingsByRoomAndDateRange(eq(1L), any(Date.class), any(Date.class)))
                .thenReturn(List.of(booking));
        when(blockedDateRepository.findByRoomIdAndBlockedDateBetween(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(blockedDate));

        RoomAvailabilityResponse expectedResponse = RoomAvailabilityResponse.builder()
                .roomId(1L)
                .roomNumber("101")
                .build();
        when(mapper.toResponse(eq(room), anyList(), anyList(), anyList()))
                .thenReturn(expectedResponse);

        // When
        RoomAvailabilityResponse result = roomAvailabilityService.getRoomAvailability(1L, startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getRoomId());
        assertEquals("101", result.getRoomNumber());
        verify(roomRepository).findById(1L);
        verify(bookingRepository).findBookingsByRoomAndDateRange(eq(1L), any(Date.class), any(Date.class));
        verify(blockedDateRepository).findByRoomIdAndBlockedDateBetween(eq(1L), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void getRoomAvailability_RoomNotFound_ThrowsException() {
        // Given
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> roomAvailabilityService.getRoomAvailability(1L, startDate, endDate));
        assertEquals(ErrorCode.ROOM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void getRoomAvailability_InvalidDateRange_ThrowsException() {
        // Given
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        LocalDate invalidEndDate = startDate.minusDays(1);

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> roomAvailabilityService.getRoomAvailability(1L, startDate, invalidEndDate));
        assertEquals(ErrorCode.INVALID_DATE_RANGE, exception.getErrorCode());
    }

    @Test
    void getAllRoomsAvailability_ValidInput_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Room> roomsPage = new PageImpl<>(List.of(room));

        when(roomRepository.findAllAvailableRooms(pageable)).thenReturn(roomsPage);
        when(bookingRepository.findBookingsByRoomAndDateRange(eq(1L), any(Date.class), any(Date.class)))
                .thenReturn(List.of());
        when(blockedDateRepository.findByRoomIdAndBlockedDateBetween(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());

        RoomAvailabilityPageResponse pageResponse = RoomAvailabilityPageResponse.builder()
                .roomId(1L)
                .roomNumber("101")
                .build();
        when(mapper.toPageResponse(eq(room), eq(startDate), eq(endDate), anyList(), anyList(), anyList()))
                .thenReturn(pageResponse);

        // When
        PaginationResponse<RoomAvailabilityPageResponse> result =
                roomAvailabilityService.getAllRoomsAvailability(startDate, endDate, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(0, result.getPage());
        assertEquals(10, result.getPageSize());
        verify(roomRepository).findAllAvailableRooms(pageable);
    }

    @Test
    void getAllRoomsAvailability_InvalidDateRange_ThrowsException() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        LocalDate invalidEndDate = startDate.minusDays(1);

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> roomAvailabilityService.getAllRoomsAvailability(startDate, invalidEndDate, pageable));
        assertEquals(ErrorCode.INVALID_DATE_RANGE, exception.getErrorCode());
    }

    @Test
    void getCalendarViewPaginated_ValidInput_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        when(roomRepository.findAllAvailableRooms()).thenReturn(List.of(room));
        when(bookingRepository.findBookingsByDateRange(any(Date.class), any(Date.class)))
                .thenReturn(List.of());
        when(blockedDateRepository.findByBlockedDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());

        RoomCalendarInfo calendarInfo = RoomCalendarInfo.builder()
                .roomId(1L)
                .roomNumber("101")
                .status("AVAILABLE")
                .bookingInfo("")
                .build();
        when(mapper.toCalendarInfo(eq(room), eq("AVAILABLE"), eq("")))
                .thenReturn(calendarInfo);

        // When
        PaginationResponse<CalendarDayResponse> result =
                roomAvailabilityService.getCalendarViewPaginated(2025, 6, pageable);

        // Then
        assertNotNull(result);
        assertFalse(result.getContent().isEmpty());
        verify(roomRepository).findAllAvailableRooms();
        verify(bookingRepository).findBookingsByDateRange(any(Date.class), any(Date.class));
    }

    @Test
    void getBlockedDates_ValidInput_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<RoomBlockedDate> blockedDatesPage = new PageImpl<>(List.of(blockedDate));

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(blockedDateRepository.findByRoomIdAndBlockedDateBetweenOrderByBlockedDate(
                eq(1L), eq(startDate), eq(endDate), eq(pageable)))
                .thenReturn(blockedDatesPage);

        // When
        PaginationResponse<RoomBlockedDate> result =
                roomAvailabilityService.getBlockedDates(1L, startDate, endDate, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(blockedDate, result.getContent().get(0));
        verify(roomRepository).findById(1L);
        verify(blockedDateRepository).findByRoomIdAndBlockedDateBetweenOrderByBlockedDate(
                eq(1L), eq(startDate), eq(endDate), eq(pageable));
    }

    @Test
    void getBlockedDates_RoomNotFound_ThrowsException() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> roomAvailabilityService.getBlockedDates(1L, startDate, endDate, pageable));
        assertEquals(ErrorCode.ROOM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void blockDates_ValidInput_Success() {
        // Given
        setupSecurityContext();
        List<LocalDate> datesToBlock = List.of(LocalDate.now().plusDays(1), LocalDate.now().plusDays(2));
        BlockDatesRequest request = BlockDatesRequest.builder()
                .blockDates(datesToBlock)
                .reason("Maintenance")
                .build();

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(bookingRepository.findBookingsByRoomAndDateRange(eq(1L), any(Date.class), any(Date.class)))
                .thenReturn(List.of());

        // When
        roomAvailabilityService.blockDates(1L, request);

        // Then
        verify(roomRepository).findById(1L);
        verify(userRepository).findByUsername("admin");
        verify(blockedDateRepository).deleteByRoomIdAndBlockedDateIn(1L, datesToBlock);
        verify(blockedDateRepository).saveAll(anyList());
    }


    @Test
    void blockDates_RoomNotFound_ThrowsException() {
        // Given
        BlockDatesRequest request = BlockDatesRequest.builder()
                .blockDates(List.of(LocalDate.now().plusDays(1)))
                .reason("Maintenance")
                .build();

        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> roomAvailabilityService.blockDates(1L, request));
        assertEquals(ErrorCode.ROOM_NOT_FOUND, exception.getErrorCode());
    }


    @Test
    void blockDates_UserNotFound_ThrowsException() {
        // Given
        setupSecurityContext();
        BlockDatesRequest request = BlockDatesRequest.builder()
                .blockDates(List.of(LocalDate.now().plusDays(1)))
                .reason("Maintenance")
                .build();

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> roomAvailabilityService.blockDates(1L, request));
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void blockDates_PastDates_ThrowsException() {
        // Given
        setupSecurityContext();
        BlockDatesRequest request = BlockDatesRequest.builder()
                .blockDates(List.of(LocalDate.now().minusDays(1)))
                .reason("Maintenance")
                .build();

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> roomAvailabilityService.blockDates(1L, request));
        assertEquals(ErrorCode.INVALID_DATE_RANGE, exception.getErrorCode());
    }

    @Test
    void blockDates_AlreadyBooked_ThrowsException() {
        // Given
        setupSecurityContext();
        LocalDate futureDate = LocalDate.now().plusDays(1);
        BlockDatesRequest request = BlockDatesRequest.builder()
                .blockDates(List.of(futureDate))
                .reason("Maintenance")
                .build();

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(bookingRepository.findBookingsByRoomAndDateRange(eq(1L), any(Date.class), any(Date.class)))
                .thenReturn(List.of(booking));

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> roomAvailabilityService.blockDates(1L, request));
        assertEquals(ErrorCode.ROOM_ALREADY_BOOKED, exception.getErrorCode());
    }

    @Test
    void unblockDates_ValidInput_Success() {
        // Given
        List<LocalDate> datesToUnblock = List.of(startDate, endDate);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        // When
        roomAvailabilityService.unblockDates(1L, datesToUnblock);

        // Then
        verify(roomRepository).findById(1L);
        verify(blockedDateRepository).deleteByRoomIdAndBlockedDateIn(1L, datesToUnblock);
    }

    @Test
    void unblockDates_RoomNotFound_ThrowsException() {
        // Given
        List<LocalDate> datesToUnblock = List.of(startDate, endDate);
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> roomAvailabilityService.unblockDates(1L, datesToUnblock));
        assertEquals(ErrorCode.ROOM_NOT_FOUND, exception.getErrorCode());
    }

    private void setupSecurityContext() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin");
    }
}