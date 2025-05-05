package project.hotel_booking_system.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import project.hotel_booking_system.dto.response.UserResponse;
import project.hotel_booking_system.model.User;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    UserResponse toUserResponse(User user);
} 