package project.hotel_booking_system.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import project.hotel_booking_system.dto.response.RoomAvailabilityPageResponse;
import project.hotel_booking_system.dto.response.RoomAvailabilityResponse;
import project.hotel_booking_system.dto.response.RoomCalendarInfo;
import project.hotel_booking_system.model.Room;

import java.time.LocalDate;
import java.util.List;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoomAvailabilityMapper {
    @Mapping(target = "roomId", source = "room.id")
    @Mapping(target = "roomNumber", source = "room.roomNumber")
    @Mapping(target = "totalAvailableDays", expression = "java(availableDates.size())")
    @Mapping(target = "totalBookedDays", expression = "java(bookedDates.size())")
    @Mapping(target = "totalBlockedDays", expression = "java(blockedDates.size())")
    RoomAvailabilityPageResponse toPageResponse(
            Room room,
            LocalDate startDate,
            LocalDate endDate,
            List<LocalDate> availableDates,
            List<LocalDate> bookedDates,
            List<LocalDate> blockedDates
    );

    @Mapping(target = "roomId", source = "room.id")
    @Mapping(target = "roomNumber", source = "room.roomNumber")
    RoomAvailabilityResponse toResponse(
            Room room,
            List<LocalDate> availableDates,
            List<LocalDate> bookedDates,
            List<LocalDate> blockedDates
    );

    @Mapping(target = "roomId", source = "room.id")
    @Mapping(target = "roomNumber", source = "room.roomNumber")
    @Mapping(target = "roomType", source = "room.roomType")
    RoomCalendarInfo toCalendarInfo(Room room, String status, String bookingInfo);
}
