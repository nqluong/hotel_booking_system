package project.hotel_booking_system.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VnPayConfig {

    @Value("${vnpay.version}")
     String version;

    @Value("${vnpay.command}")
     String command;

    @Value("${vnpay.tmnCode}")
     String tmnCode;

    @Value("${vnpay.hashSecret}")
     String hashSecret;

    @Value("${vnpay.currCode}")
     String currCode;

    @Value("${vnpay.locale}")
    String locale;

    @Value("${vnpay.paymentUrl}")
    String paymentUrl;

    @Value("${vnpay.returnUrl}")
    String returnUrl;

    @Value("${vnpay.orderType}")
    String orderType;

} 