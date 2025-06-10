package project.hotel_booking_system.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.hotel_booking_system.model.Review;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByRoomIdOrderByCreatedAtDesc(Long roomId, Pageable pageable);

    Page<Review> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    boolean existsByUserIdAndRoomId(Long userId, Long roomId);

    Optional<Review> findByUserIdAndRoomId(Long userId, Long roomId);

    // Sum of ratings for a specific room
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.room.id = :roomId")
    Optional<Double> findAverageRatingByRoomId(@Param("roomId") Long roomId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.room.id = :roomId")
    Long countByRoomId(@Param("roomId") Long roomId);

    // Statistics by rating level
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.room.id = :roomId GROUP BY r.rating")
    List<Object[]> findRatingDistributionByRoomId(@Param("roomId") Long roomId);

    // Top rated rooms
    @Query("SELECT r.room.id, AVG(r.rating) as avgRating, COUNT(r) as reviewCount " +
            "FROM Review r GROUP BY r.room.id " +
            "HAVING COUNT(r) >= :minReviews ORDER BY avgRating DESC")
    List<Object[]> findTopRatedRooms(@Param("minReviews") Long minReviews, Pageable pageable);
}
