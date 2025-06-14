package project.hotel_booking_system.configuration;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;
import project.hotel_booking_system.enums.Role;
import project.hotel_booking_system.model.User;
import project.hotel_booking_system.repository.UserRepository;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {
    PasswordEncoder passwordEncoder;

    @NonFinal
    static final String ADMIN_USER_NAME = "admin";

    @NonFinal
    static final String ADMIN_PASSWORD = "admin";

    @NonFinal
    static final String ADMIN_EMAIL = "adminhotel@gmail.com";

    @NonFinal
    static final String ADMIN_PHONE = "0123456789";

    @NonFinal
    static final String ADMIN_FULL_NAME = "Admin User";


    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository) {
        log.info("Initializing application.....");
        return args -> {
            if (userRepository.findByUsername(ADMIN_USER_NAME).isEmpty()) {
                userRepository.save(User.builder()
                        .username(ADMIN_USER_NAME)
                        .password(passwordEncoder.encode(ADMIN_PASSWORD))
                        .email(ADMIN_EMAIL)
                        .phone(ADMIN_PHONE)
                        .fullname(ADMIN_FULL_NAME)
                        .isActive(true)
                        .createAt(LocalDateTime.now())
                        .role(Role.ADMIN)
                        .build());
                log.info("Admin user created with username: {}", ADMIN_USER_NAME);
            } else {
                log.info("Admin user already exists.");
            }
        };

    }
}
