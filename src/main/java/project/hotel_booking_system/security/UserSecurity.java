package project.hotel_booking_system.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import project.hotel_booking_system.repository.ReviewRepository;

@Slf4j
@Component("userSecurity")
public class UserSecurity {

    public boolean isCurrentUser(Long userId) {
        Long currentUserId = getCurrentUserId();
        return userId != null && userId.equals(currentUserId);
    }

    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        try {
            if (authentication.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getPrincipal();


                Long userId = jwt.getClaim("userId");
                if (userId != null) {
                    return userId;
                }


                Object subClaim = jwt.getClaim("sub");
                if (subClaim instanceof Number) {
                    return ((Number) subClaim).longValue();
                } else if (subClaim instanceof String) {
                    try {
                        return Long.valueOf((String) subClaim);
                    } catch (NumberFormatException e) {
                        log.warn("Cannot parse sub claim as Long: {}", subClaim);
                        return null;
                    }
                }
            }
            return null;
        } catch (Exception e) {
            log.error("Error getting current user ID: ", e);
            return null;
        }
    }

    public boolean isReviewOwner(Long reviewId, ReviewRepository reviewRepository) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return false;
        }

        try {
            return reviewRepository.findById(reviewId)
                    .map(review -> review.getUser().getId().equals(currentUserId))
                    .orElse(false);
        } catch (Exception e) {
            log.error("Error checking review ownership: ", e);
            return false;
        }
    }
}