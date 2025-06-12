package project.hotel_booking_system.service.room;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoomAvailabilityServiceImpl implements RoomAvailabilityService {

    RoomRepository roomRepository;
    BookingRepository bookingRepository;
    RoomBlockedDateRepository blockedDateRepository;
    UserRepository userRepository;
    RoomAvailabilityMapper roomAvailabilityMapper;

    @Override
    public RoomAvailabilityResponse getRoomAvailability(Long roomId, LocalDate startDate, LocalDate endDate) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));

        if (startDate.isAfter(endDate)) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }

        List<LocalDate> allDates = startDate.datesUntil(endDate.plusDays(1)).toList();

        List<Booking> bookings = bookingRepository.findBookingsByRoomAndDateRange(
                roomId, Date.valueOf(startDate), Date.valueOf(endDate));

        List<LocalDate> bookedDates = bookings.stream()
                .flatMap(booking -> {
                    LocalDate checkIn = booking.getCheckInDate().toInstant()
                            .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                    LocalDate checkOut = booking.getCheckOutDate().toInstant()
                            .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                    return checkIn.datesUntil(checkOut);
                })
                .distinct()
                .toList();

        List<RoomBlockedDate> blockedDateEntities = blockedDateRepository
                .findByRoomIdAndBlockedDateBetween(roomId, startDate, endDate);

        List<LocalDate> blockedDates = blockedDateEntities.stream()
                .map(RoomBlockedDate::getBlockedDate)
                .toList();

        List<LocalDate> unavailableDates = new ArrayList<>();
        unavailableDates.addAll(bookedDates);
        unavailableDates.addAll(blockedDates);

        List<LocalDate> availableDates = allDates.stream()
                .filter(date -> !unavailableDates.contains(date))
                .toList();

        return roomAvailabilityMapper.toResponse(room, availableDates, bookedDates, blockedDates);
    }


    @Override
    public PaginationResponse<RoomAvailabilityPageResponse> getAllRoomsAvailability(
            LocalDate startDate, LocalDate endDate, Pageable pageable) {

        if (startDate.isAfter(endDate)) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }

        Page<Room> roomsPage = roomRepository.findAllAvailableRooms(pageable);

        List<RoomAvailabilityPageResponse> content = roomsPage.getContent().stream()
                .map(room -> {
                    List<LocalDate> allDates = startDate.datesUntil(endDate.plusDays(1)).toList();

                    List<Booking> bookings = bookingRepository.findBookingsByRoomAndDateRange(
                            room.getId(), Date.valueOf(startDate), Date.valueOf(endDate));

                    List<LocalDate> bookedDates = bookings.stream()
                            .flatMap(booking -> {
                                LocalDate checkIn = booking.getCheckInDate().toInstant()
                                        .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                                LocalDate checkOut = booking.getCheckOutDate().toInstant()
                                        .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                                return checkIn.datesUntil(checkOut);
                            })
                            .distinct()
                            .toList();

                    List<RoomBlockedDate> blockedDateEntities = blockedDateRepository
                            .findByRoomIdAndBlockedDateBetween(room.getId(), startDate, endDate);

                    List<LocalDate> blockedDates = blockedDateEntities.stream()
                            .map(RoomBlockedDate::getBlockedDate)
                            .toList();

                    List<LocalDate> unavailableDates = new ArrayList<>();
                    unavailableDates.addAll(bookedDates);
                    unavailableDates.addAll(blockedDates);

                    List<LocalDate> availableDates = allDates.stream()
                            .filter(date -> !unavailableDates.contains(date))
                            .toList();

                    return roomAvailabilityMapper.toPageResponse(room, startDate, endDate,
                            availableDates, bookedDates, blockedDates);
                })
                .toList();

        return PaginationResponse.<RoomAvailabilityPageResponse>builder()
                .content(content)
                .page(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalElements(roomsPage.getTotalElements())
                .totalPages(roomsPage.getTotalPages())
                .last(roomsPage.isLast())
                .build();
    }



    @Override
    public PaginationResponse<CalendarDayResponse> getCalendarViewPaginated(
            int year, int month, Pageable pageable) {

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<LocalDate> allDates = startDate.datesUntil(endDate.plusDays(1)).toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allDates.size());
        List<LocalDate> paginatedDates = allDates.subList(start, end);

        List<Room> rooms = roomRepository.findAllAvailableRooms();
        List<Booking> bookings = bookingRepository.findBookingsByDateRange(
                Date.valueOf(paginatedDates.get(0)),
                Date.valueOf(paginatedDates.get(paginatedDates.size() - 1)));
        List<RoomBlockedDate> blockedDates = blockedDateRepository
                .findByBlockedDateBetween(paginatedDates.get(0),
                        paginatedDates.get(paginatedDates.size() - 1));

        List<CalendarDayResponse> content = paginatedDates.stream()
                .map(date -> CalendarDayResponse.builder()
                        .date(date)
                        .rooms(getRoomCalendarInfoForDate(rooms, bookings, blockedDates, date))
                        .build())
                .toList();

        return PaginationResponse.<CalendarDayResponse>builder()
                .content(content)
                .page(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalElements(allDates.size())
                .totalPages((int) Math.ceil((double) allDates.size() / pageable.getPageSize()))
                .last(end >= allDates.size())
                .build();
    }

    @Override
    public PaginationResponse<RoomBlockedDate> getBlockedDates(
            Long roomId, LocalDate startDate, LocalDate endDate, Pageable pageable) {

        roomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));

        Page<RoomBlockedDate> blockedDatesPage = blockedDateRepository
                .findByRoomIdAndBlockedDateBetweenOrderByBlockedDate(
                        roomId, startDate, endDate, pageable);

        return PaginationResponse.<RoomBlockedDate>builder()
                .content(blockedDatesPage.getContent())
                .page(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalElements(blockedDatesPage.getTotalElements())
                .totalPages(blockedDatesPage.getTotalPages())
                .last(blockedDatesPage.isLast())
                .build();
    }

    private List<RoomCalendarInfo> getRoomCalendarInfoForDate(
            List<Room> rooms, List<Booking> bookings,
            List<RoomBlockedDate> blockedDates, LocalDate date) {

        return rooms.stream()
                .map(room -> {
                    Optional<Booking> booking = bookings.stream()
                            .filter(b -> b.getRoom().getId().equals(room.getId()))
                            .filter(b -> {
                                LocalDate checkIn = b.getCheckInDate().toInstant()
                                        .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                                LocalDate checkOut = b.getCheckOutDate().toInstant()
                                        .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                                return !date.isBefore(checkIn) && date.isBefore(checkOut);
                            })
                            .findFirst();

                    boolean isBlocked = blockedDates.stream()
                            .anyMatch(bd -> bd.getRoom().getId().equals(room.getId())
                                    && bd.getBlockedDate().equals(date));

                    String status;
                    String bookingInfo;

                    if (booking.isPresent()) {
                        status = "BOOKED";
                        bookingInfo = "Booking #" + booking.get().getId() +
                                " - " + booking.get().getUser().getFullname();
                    } else if (isBlocked) {
                        status = "BLOCKED";
                        bookingInfo = "Maintenance/Blocked";
                    } else {
                        status = "AVAILABLE";
                        bookingInfo = "";
                    }

                    return roomAvailabilityMapper.toCalendarInfo(room, status, bookingInfo);
                })
                .toList();
    }

    //Block and unblock dates for a room
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    @Override
    public void blockDates(Long roomId, BlockDatesRequest request) {
        // Validate room exists
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));


        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User admin = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Validate dates are not in the past
        LocalDate today = LocalDate.now();
        List<LocalDate> invalidDates = request.getBlockDates().stream()
                .filter(date -> date.isBefore(today))
                .toList();

        if (!invalidDates.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }

        // Check if any dates are already booked
        for (LocalDate date : request.getBlockDates()) {
            List<Booking> existingBookings = bookingRepository.findBookingsByRoomAndDateRange(
                    roomId,
                    Date.valueOf(date),
                    Date.valueOf(date.plusDays(1))
            );

            if (!existingBookings.isEmpty()) {
                throw new AppException(ErrorCode.ROOM_ALREADY_BOOKED);
            }
        }

        blockedDateRepository.deleteByRoomIdAndBlockedDateIn(roomId, request.getBlockDates());

        List<RoomBlockedDate> blockedDates = request.getBlockDates().stream()
                .map(date -> RoomBlockedDate.builder()
                        .room(room)
                        .blockedDate(date)
                        .reason(request.getReason())
                        .createdAt(LocalDateTime.now())
                        .createdBy(admin.getId())
                        .build())
                .toList();

        blockedDateRepository.saveAll(blockedDates);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    @Override
    public void unblockDates(Long roomId, List<LocalDate> dates) {
        // Validate room exists
        roomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));

        blockedDateRepository.deleteByRoomIdAndBlockedDateIn(roomId, dates);
    }
}