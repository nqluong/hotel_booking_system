package project.hotel_booking_system.mapper;

import java.time.LocalDateTime;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import project.hotel_booking_system.dto.request.UserCreateRequest;
import project.hotel_booking_system.dto.response.UserResponse;
import project.hotel_booking_system.enums.Role;
import project.hotel_booking_system.model.User;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface  UserMapper {
    UserResponse toUserResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true) // Ignore password, sẽ set trong service
    @Mapping(target = "role", constant = "CUSTOMER")
    @Mapping(target = "createAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "isActive", constant = "true")
    User toUser(UserCreateRequest request);

} 