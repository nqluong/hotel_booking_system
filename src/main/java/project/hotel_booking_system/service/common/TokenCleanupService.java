package project.hotel_booking_system.service.common;

import java.time.Instant;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.hotel_booking_system.repository.InvalidatedTokenRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenCleanupService {

    private final InvalidatedTokenRepository invalidatedTokenRepository;

    @Value("${jwt.refreshable-duration}")
    private long refreshableDuration;

    @Scheduled(cron = "0 0 * * * *")
    public void cleanupExpiredTokens() {
        try {
            Date expirationThreshold = Date.from(Instant.now().minusSeconds(refreshableDuration));
            long deletedCount = invalidatedTokenRepository.deleteByInvalidatedAtBefore(expirationThreshold);
            log.info("Cleaned up {} expired tokens", deletedCount);
        } catch (Exception e) {
            log.error("Error during token cleanup: {}", e.getMessage());
        }
    }
} 