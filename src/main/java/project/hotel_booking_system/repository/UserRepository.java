package project.hotel_booking_system.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import project.hotel_booking_system.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsernameAndIsActiveTrue(String username);
    
    Optional<User> findByEmailAndIsActiveTrue(String email);
    
    @Query("SELECT u FROM User u WHERE u.isActive = true AND (u.username = :identifier OR u.email = :identifier)")
    Optional<User> findByUsernameOrEmailAndIsActiveTrue(@Param("identifier") String identifier);
}
