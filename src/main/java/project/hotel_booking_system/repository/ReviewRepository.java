package project.hotel_booking_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.hotel_booking_system.model.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
}
