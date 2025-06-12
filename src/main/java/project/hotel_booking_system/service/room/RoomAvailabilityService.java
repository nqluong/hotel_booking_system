package project.hotel_booking_system.service.room;

import org.springframework.data.domain.Pageable;
import project.hotel_booking_system.dto.request.room_request.BlockDatesRequest;
import project.hotel_booking_system.dto.response.*;
import project.hotel_booking_system.model.RoomBlockedDate;

import java.time.LocalDate;
import java.util.List;

public interface RoomAvailabilityService {

    RoomAvailabilityResponse getRoomAvailability(Long roomId, LocalDate startDate, LocalDate endDate);

    PaginationResponse<RoomAvailabilityPageResponse> getAllRoomsAvailability(
            LocalDate startDate, LocalDate endDate, Pageable pageable);

    PaginationResponse<CalendarDayResponse> getCalendarViewPaginated(
            int year, int month, Pageable pageable);

    void blockDates(Long roomId, BlockDatesRequest request);

    void unblockDates(Long roomId, List<LocalDate> dates);

    PaginationResponse<RoomBlockedDate> getBlockedDates(
            Long roomId, LocalDate startDate, LocalDate endDate, Pageable pageable);
}
