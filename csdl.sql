-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               9.1.0 - MySQL Community Server - GPL
-- Server OS:                    Win64
-- HeidiSQL Version:             12.8.0.6908
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- Dumping database structure for hotel_booking_system
CREATE DATABASE IF NOT EXISTS `hotel_booking_system` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `hotel_booking_system`;

-- Dumping structure for table hotel_booking_system.bookings
CREATE TABLE IF NOT EXISTS `bookings` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `room_id` bigint NOT NULL,
  `check_in_date` date NOT NULL,
  `check_out_date` date NOT NULL,
  `status` enum('CONFIRMED','CANCELLED','COMPLETED') NOT NULL DEFAULT 'CONFIRMED',
  `total_price` decimal(10,2) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  KEY `room_id` (`room_id`),
  CONSTRAINT `bookings_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `bookings_ibfk_2` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table hotel_booking_system.bookings: ~10 rows (approximately)
INSERT INTO `bookings` (`id`, `user_id`, `room_id`, `check_in_date`, `check_out_date`, `status`, `total_price`, `created_at`) VALUES
	(1, 2, 2, '2024-12-30', '2025-01-02', 'CONFIRMED', 225.00, '2024-12-26 16:47:53'),
	(2, 3, 3, '2024-12-31', '2025-01-03', 'CANCELLED', 450.00, '2024-12-26 16:47:53'),
	(3, 4, 4, '2025-01-01', '2025-01-04', 'COMPLETED', 165.00, '2024-12-26 16:47:53'),
	(4, 5, 5, '2025-01-02', '2025-01-05', 'CONFIRMED', 240.00, '2024-12-26 16:47:53'),
	(5, 6, 6, '2025-01-03', '2025-01-06', 'COMPLETED', 465.00, '2024-12-26 16:47:53'),
	(6, 7, 7, '2025-01-04', '2025-01-07', 'CANCELLED', 180.00, '2024-12-26 16:47:53'),
	(7, 8, 8, '2025-01-05', '2025-01-08', 'CONFIRMED', 255.00, '2024-12-26 16:47:53'),
	(8, 9, 9, '2025-01-06', '2025-01-09', 'COMPLETED', 480.00, '2024-12-26 16:47:53'),
	(9, 10, 10, '2025-01-07', '2025-01-10', 'CONFIRMED', 195.00, '2024-12-26 16:47:53'),
	(10, 2, 1, '2025-01-08', '2025-01-11', 'COMPLETED', 50.00, '2024-12-26 16:47:53');

-- Dumping structure for table hotel_booking_system.payments
CREATE TABLE IF NOT EXISTS `payments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `booking_id` bigint NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `payment_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `payment_method` enum('CREDIT_CARD','PAYPAL','CASH') NOT NULL,
  `status` enum('PENDING','PAID','FAILED') NOT NULL DEFAULT 'PENDING',
  PRIMARY KEY (`id`),
  KEY `booking_id` (`booking_id`),
  CONSTRAINT `payments_ibfk_1` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table hotel_booking_system.payments: ~10 rows (approximately)
INSERT INTO `payments` (`id`, `booking_id`, `amount`, `payment_date`, `payment_method`, `status`) VALUES
	(1, 1, 225.00, '2024-12-26 16:48:17', 'CREDIT_CARD', 'PAID'),
	(2, 2, 450.00, '2024-12-26 16:48:17', 'PAYPAL', 'FAILED'),
	(3, 3, 165.00, '2024-12-26 16:48:17', 'CASH', 'PAID'),
	(4, 4, 240.00, '2024-12-26 16:48:17', 'CREDIT_CARD', 'PENDING'),
	(5, 5, 465.00, '2024-12-26 16:48:17', 'PAYPAL', 'PAID'),
	(6, 6, 180.00, '2024-12-26 16:48:17', 'CASH', 'FAILED'),
	(7, 7, 255.00, '2024-12-26 16:48:17', 'CREDIT_CARD', 'PENDING'),
	(8, 8, 480.00, '2024-12-26 16:48:17', 'PAYPAL', 'PAID'),
	(9, 9, 195.00, '2024-12-26 16:48:17', 'CASH', 'FAILED'),
	(10, 10, 50.00, '2024-12-26 16:48:17', 'CREDIT_CARD', 'PAID');

-- Dumping structure for table hotel_booking_system.reviews
CREATE TABLE IF NOT EXISTS `reviews` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `room_id` bigint NOT NULL,
  `rating` tinyint NOT NULL,
  `comment` text,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  KEY `room_id` (`room_id`),
  CONSTRAINT `reviews_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `reviews_ibfk_2` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`id`) ON DELETE CASCADE,
  CONSTRAINT `reviews_chk_1` CHECK ((`rating` between 1 and 5))
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table hotel_booking_system.reviews: ~10 rows (approximately)
INSERT INTO `reviews` (`id`, `user_id`, `room_id`, `rating`, `comment`, `created_at`) VALUES
	(1, 2, 1, 5, 'Excellent room with great view.', '2024-12-26 16:51:30'),
	(2, 2, 2, 4, 'Great room but a bit noisy.', '2024-12-26 16:51:30'),
	(3, 2, 3, 3, 'Room was okay, nothing special.', '2024-12-26 16:51:30'),
	(4, 2, 1, 4, 'Nice room, comfortable stay.', '2024-12-26 16:51:30'),
	(5, 2, 2, 2, 'Not very clean, disappointed.', '2024-12-26 16:51:30'),
	(6, 2, 3, 5, 'Luxurious room with excellent service.', '2024-12-26 16:51:30'),
	(7, 2, 1, 3, 'Average room, decent service.', '2024-12-26 16:51:30'),
	(8, 2, 2, 5, 'Loved the room, will come back again.', '2024-12-26 16:51:30'),
	(9, 2, 3, 4, 'Great experience overall.', '2024-12-26 16:51:30'),
	(10, 2, 1, 1, 'Terrible experience, will not return.', '2024-12-26 16:51:30');

-- Dumping structure for table hotel_booking_system.roomimages
CREATE TABLE IF NOT EXISTS `roomimages` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` bigint NOT NULL,
  `image_url` varchar(255) NOT NULL,
  `image_type` enum('THUMBNAIL','GALLERY') DEFAULT 'GALLERY',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `room_id` (`room_id`),
  CONSTRAINT `roomimages_ibfk_1` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table hotel_booking_system.roomimages: ~10 rows (approximately)
INSERT INTO `roomimages` (`id`, `room_id`, `image_url`, `image_type`, `created_at`) VALUES
	(11, 1, 'room101_thumbnail.jpg', 'THUMBNAIL', '2024-12-26 16:55:59'),
	(12, 1, 'room101_gallery1.jpg', 'GALLERY', '2024-12-26 16:55:59'),
	(13, 1, 'room101_gallery2.jpg', 'GALLERY', '2024-12-26 16:55:59'),
	(14, 2, 'room102_thumbnail.jpg', 'THUMBNAIL', '2024-12-26 16:55:59'),
	(15, 2, 'room102_gallery1.jpg', 'GALLERY', '2024-12-26 16:55:59'),
	(16, 2, 'room102_gallery2.jpg', 'GALLERY', '2024-12-26 16:55:59'),
	(17, 3, 'room103_thumbnail.jpg', 'THUMBNAIL', '2024-12-26 16:55:59'),
	(18, 3, 'room103_gallery1.jpg', 'GALLERY', '2024-12-26 16:55:59'),
	(19, 3, 'room103_gallery2.jpg', 'GALLERY', '2024-12-26 16:55:59'),
	(20, 3, 'room103_gallery3.jpg', 'GALLERY', '2024-12-26 16:55:59');

-- Dumping structure for table hotel_booking_system.rooms
CREATE TABLE IF NOT EXISTS `rooms` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_number` varchar(20) NOT NULL,
  `type` enum('SINGLE','DOUBLE','SUITE') NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `status` enum('AVAILABLE','BOOKED','MAINTENANCE') NOT NULL DEFAULT 'AVAILABLE',
  `description` text,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `room_number` (`room_number`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table hotel_booking_system.rooms: ~10 rows (approximately)
INSERT INTO `rooms` (`id`, `room_number`, `type`, `price`, `status`, `description`, `created_at`) VALUES
	(1, '101', 'SINGLE', 50.00, 'AVAILABLE', 'Single room with one bed.', '2024-12-26 16:47:05'),
	(2, '102', 'DOUBLE', 75.00, 'BOOKED', 'Double room with two beds.', '2024-12-26 16:47:05'),
	(3, '103', 'SUITE', 150.00, 'MAINTENANCE', 'Suite room with luxurious amenities.', '2024-12-26 16:47:05'),
	(4, '104', 'SINGLE', 55.00, 'AVAILABLE', 'Single room with garden view.', '2024-12-26 16:47:05'),
	(5, '105', 'DOUBLE', 80.00, 'AVAILABLE', 'Double room with sea view.', '2024-12-26 16:47:05'),
	(6, '106', 'SUITE', 155.00, 'BOOKED', 'Suite room with private balcony.', '2024-12-26 16:47:05'),
	(7, '107', 'SINGLE', 60.00, 'AVAILABLE', 'Single room with modern decor.', '2024-12-26 16:47:05'),
	(8, '108', 'DOUBLE', 85.00, 'MAINTENANCE', 'Double room with city view.', '2024-12-26 16:47:05'),
	(9, '109', 'SUITE', 160.00, 'AVAILABLE', 'Suite room with jacuzzi.', '2024-12-26 16:47:05'),
	(10, '110', 'SINGLE', 65.00, 'BOOKED', 'Single room with king-sized bed.', '2024-12-26 16:47:05');

-- Dumping structure for table hotel_booking_system.users
CREATE TABLE IF NOT EXISTS `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `email` varchar(100) NOT NULL,
  `fullname` varchar(255) NOT NULL,
  `phone` varchar(15) NOT NULL,
  `role` enum('ADMIN','CUSTOMER') NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table hotel_booking_system.users: ~10 rows (approximately)
INSERT INTO `users` (`id`, `username`, `password`, `email`, `fullname`, `phone`, `role`, `created_at`) VALUES
	(1, 'admin', 'hashedpassword1', 'admin@hotel.com', 'Admin User', '1234567890', 'ADMIN', '2024-12-26 16:46:48'),
	(2, 'john_doe', 'hashedpassword2', 'john@example.com', 'John Doe', '0987654321', 'CUSTOMER', '2024-12-26 16:46:48'),
	(3, 'jane_doe', 'hashedpassword3', 'jane@example.com', 'Jane Doe', '0987654322', 'CUSTOMER', '2024-12-26 16:46:48'),
	(4, 'alice', 'hashedpassword4', 'alice@example.com', 'Alice Smith', '0987654323', 'CUSTOMER', '2024-12-26 16:46:48'),
	(5, 'bob', 'hashedpassword5', 'bob@example.com', 'Bob Johnson', '0987654324', 'CUSTOMER', '2024-12-26 16:46:48'),
	(6, 'charlie', 'hashedpassword6', 'charlie@example.com', 'Charlie Brown', '0987654325', 'CUSTOMER', '2024-12-26 16:46:48'),
	(7, 'david', 'hashedpassword7', 'david@example.com', 'David Wilson', '0987654326', 'CUSTOMER', '2024-12-26 16:46:48'),
	(8, 'eve', 'hashedpassword8', 'eve@example.com', 'Eve Miller', '0987654327', 'CUSTOMER', '2024-12-26 16:46:48'),
	(9, 'frank', 'hashedpassword9', 'frank@example.com', 'Frank Moore', '0987654328', 'CUSTOMER', '2024-12-26 16:46:48'),
	(10, 'grace', 'hashedpassword10', 'grace@example.com', 'Grace Taylor', '0987654329', 'CUSTOMER', '2024-12-26 16:46:48');

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
