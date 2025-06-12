# 🏨 Hotel Booking System

A comprehensive hotel booking management system built with Spring Boot, providing robust APIs for room reservations, user authentication, payment processing, reviews, and room availability management.

## 📋 Overview

This application is a modern hotel booking management system featuring high security, online payment integration, role-based user management, customer reviews, and advanced room availability tracking designed for both customers and hotel administrators.

## ✨ Key Features

- 🔐 **User Authentication & Authorization** - Secure JWT-based authentication
- 🏠 **Room Booking Management** - Complete reservation lifecycle management
- 💳 **VNPay Payment Integration** - Secure online payment processing
- 👥 **Role-based Access Control** - Admin and Customer role separation
- ⭐ **Review System** - Customer reviews and ratings for rooms
- 📅 **Room Availability Management** - Advanced calendar and availability tracking
- 🚫 **Room Blocking** - Block specific dates for maintenance or other purposes
- 📚 **API Documentation** - Interactive Swagger/OpenAPI documentation
- 🛡️ **Security First** - Industry-standard security practices

## 🛠️ Technology Stack

| Category | Technology |
|----------|------------|
| **Backend** | Java 17+, Spring Boot 3.x |
| **Security** | Spring Security, JWT Authentication |
| **Database** | MySQL 8.0+, Spring Data JPA |
| **Payment** | VNPay Gateway Integration |
| **Documentation** | Swagger/OpenAPI 3 |
| **Build Tool** | Maven 3.6+ |

## 📋 System Requirements

- ☕ **Java 17** or higher
- 🗄️ **MySQL 8.0** or higher
- 📦 **Maven 3.6** or higher
- 💻 **IDE** (IntelliJ IDEA recommended)

## 🚀 Quick Start

### 1. Clone Repository
```bash
git clone https://github.com/nqluong/hotel-booking-system.git
cd hotel-booking-system
```

### 2. Environment Configuration

Create a `.env` file in the root directory:

```env
# Database Configuration
DBMS_PORT=3306
DBMS_NAME=hotel_booking
DBMS_USERNAME=your_db_username
DBMS_PASSWORD=your_db_password

# VNPay Payment Configuration
TMN_CODE=your_vnpay_tmn_code
PAY_URL=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
SECRET_KEY=your_vnpay_secret_key
RETURN_URL=http://localhost:8080/hotelbooking/payments/vnpay-callback

# JWT Configuration
SIGNER_KEY=your_jwt_secret_key
```

### 3. Database Setup

1. Create MySQL database:
```sql
CREATE DATABASE hotel_booking;
```

2. Update `application.yml` configuration:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:${DBMS_PORT}/${DBMS_NAME}
    username: ${DBMS_USERNAME}
    password: ${DBMS_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

### 4. Build & Run

```bash
# Install dependencies
mvn clean install

# Run the application
mvn spring-boot:run
```

🌐 **Application URL**: `http://localhost:8080/hotelbooking`

## 📖 API Documentation

Interactive API documentation is available at:
- **Swagger UI**: `http://localhost:8080/hotelbooking/swagger-ui.html`
- **OpenAPI Spec**: `http://localhost:8080/hotelbooking/v3/api-docs`

### 🔑 Authentication Endpoints (`/auth`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/auth/token` | User login |
| `POST` | `/auth/refresh` | Refresh access token |
| `POST` | `/auth/logout` | User logout |
| `POST` | `/auth/introspect` | Validate token |

### 👤 User Management Endpoints(`/users`)

|  Method  | Endpoint                   |     Description     |  Access Level  |
|:--------:|----------------------------|:-------------------:|:--------------:|
|  `POST`  | `/users`                   | Register a new user |     Public     |
|  `GET`   | `/users`                   |    Get all users    |   Admin Only   |
|  `GET`   | `/users/{id}`              |   Get user by ID    |   Admin/Self   |
|  `PUT`   | `/users/{id}`        | Update user information |   Admin/Self   |
| `DELETE` | `/users/{id}`        |     Delete user     |   Admin Only   |
| `PATCH`  | `/users/{id}/status` |  Update user role   |   Admin Only   |

### 🏠 Room Management Endpoints(`/rooms`)

| Method   | Endpoint          | Description                | Access Level |
|----------|-------------------|----------------------------|--------------|
| `GET`    | `/rooms`          | Get all rooms              | Public       |
| `GET`    | `/rooms/{roomId}` | Get room by ID             | Public       |
| `POST`   | `/rooms`          | Create new room            | Admin Only   |
| `POST`   | `/rooms/search`   | Search for available rooms | Public       |
| `PUT`    | `/rooms/{roomId}` | Update room                | Admin Only   |
| `DELETE` | `/rooms/types`    | Delete room                | Admin Only   |

### 📅 Room Availability Management Endpoints (`/rooms`)

| Method | Endpoint | Description | Access Level |
|--------|----------|-------------|--------------|
| `GET` | `/rooms/{roomId}/availability` | Get room availability for date range | Public |
| `GET` | `/rooms/availability` | Get availability for all rooms (paginated) | Public |
| `GET` | `/rooms/availability/calendar` | Get paginated calendar view | Public |
| `GET` | `/rooms/{roomId}/blocked-dates` | Get blocked dates for a room | Public |
| `GET` | `/rooms/{roomId}/availability/quick` | Quick availability check for specific dates | Public |
| `PUT` | `/rooms/{roomId}/block-dates` | Block dates for a room | Admin Only |
| `DELETE` | `/rooms/{roomId}/block-dates` | Unblock dates for a room | Admin Only |

**Availability Parameters:**
- `startDate`: Start date (ISO format: YYYY-MM-DD)
- `endDate`: End date (ISO format: YYYY-MM-DD)
- `year`: Year for calendar view
- `month`: Month for calendar view (1-12)
- `page`: Page number (0-based)
- `size`: Number of items per page
- `sortBy`: Field to sort by (default: roomNumber)
- `sortDir`: Sort direction (asc/desc)

### 📷 Room Image Management Endpoints (`/room-images`)

| Method | Endpoint | Description | Access Level |
|--------|----------|-------------|--------------|
| `POST` | `/room-images/upload/{roomId}` | Upload room image | Admin Only |
| `GET` | `/room-images/{roomId}` | Get all images by room ID | Public |
| `DELETE` | `/room-images/{roomId}/images/{imageId}` | Delete room image | Admin Only |
| `PUT` | `/room-images/{roomId}/images/{imageId}` | Update room image | Admin Only |
| `PUT` | `/room-images/{roomId}/images/{imageId}/type` | Update image type | Admin Only |

**Image Upload Parameters:**
- `file`: Image file (multipart/form-data)
- `imageType`: Type of image (THUMBNAIL, GALLERY, etc.)

### ⭐ Review Management Endpoints (`/reviews`)

| Method | Endpoint | Description | Access Level |
|----|----------|-------------|--------------|
| `POST` | `/reviews` | Create a new review | Customer Only |
| `PUT` | `/reviews/{reviewId}` | Update an existing review | Review Owner |
| `DELETE` | `/reviews/update/{reviewId}` | Delete a review | Review Owner |
| `GET` | `/reviews/room/{roomId}` | Get reviews for a room | Public |
| `GET` | `/reviews/my-reviews` | Get current user's reviews | Customer Only |
| `GET` | `/reviews/room/{roomId}/summary` | Get room review summary | Public |
| `GET` | `/reviews/{reviewId}` | Get review by ID | Public |
| `GET` | `/reviews/admin/all` | Get all reviews (paginated) | Admin Only |
| `DELETE` | `/reviews/admin/{reviewId}` | Delete any review | Admin Only |


**Review System Features:**
- Users can only review rooms they have completed bookings for
- One review per user per room
- Rating scale: 1-5 stars
- Review summary with rating distribution
- Pagination support for all endpoints

### 📅 Booking Management Endpoints

#### Customer Booking APIs (`/bookings`)
| Method | Endpoint | Description | Access Level  |
|--------|----------|-------------|---------------|
| `POST` | `/bookings` | Create new booking | Authenticated |
| `GET` | `/bookings` | Get user's bookings | Authenticated |
| `GET` | `/bookings/{id}` | Get booking details | Owner/Admin   |

#### Admin Booking Management (`/admin/bookings`)
| Method | Endpoint                         | Description            | Access Level |
|--------|----------------------------------|------------------------|--------------|
| `GET`  | `/admin/bookings`                | Get all bookings       | Admin Only   |
| `GET`  | `/admin/bookings/{id}`           | Get booking by ID      | Admin Only   |
| `GET`  | `/admin/bookings/user/{userId}`  | Get user bookings      | Admin Only   |
| `GET`  | `/admin/booking/status/{status}` | Get bookings by status | Admin Only   |
| `PUT`  | `/admin/bookings/{id}/status`    | Update booking status  | Admin Only   |
| `PUT`  | `/admin/bookings/{id}/confirm`   | Confirm bookings       | Admin Only   |
| `PUT`  | `/admin/booking/{id}/check-in`   | Check-in guest         | Admin Only   |
| `PUT`  | `/admin/booking/{id}/check-out`  | Check-out guest        | Admin Only   |
| `PUT`  | `/admin/booking/{id}/cancel`     | Cancel booking         | Admin Only   |

### 💳 Payment Endpoints

#### Customer Payment APIs (`/payments`)
| Method | Endpoint                            | Description              | Access Level   |
|--------|-------------------------------------|--------------------------|----------------|
| `POST` | `/payments/process-payment`         | Process VNPay payment    | Authenticated  |
| `POST` | `/payments/process-checkout-payment` | Process checkout payment | Authenticated  |
| `POST` | `/payments/cash`                    | Process cash payment     | Authenticated  |
| `GET`  | `/payments/{id}`                    | Get payment details      | Authenticated  |
| `GET`  | `/payments/booking/{bookingId}`     | Get booking payments     | Authenticated  |
| `GET`  | `/payments/vnpay-callback`          | VNPay payment callback   | Public         |
| `GET`  | `/payments/booking/{bookingId}`     | Get booking payment      | Authenticatied |

#### Admin Payment Management (`/admin/payments`)
| Method | Endpoint                                 | Description               | Access Level |
| ------ | ---------------------------------------- | ------------------------- | ------------ |
| `GET`  | `/admin/payments`                        | Get all payments          | Admin Only   |
| `GET`  | `/admin/payments/{id}`                   | Get payment by ID         | Admin Only   |
| `GET`  | `/admin/payments/status/{status}`        | Get payments by status    | Admin Only   |
| `PUT`  | `/admin/payments/{id}/status`            | Update payment status     | Admin Only   |
| `PUT`  | `/admin/payments/{id}/mark-as-completed` | Mark payment as completed | Admin Only   |
| `PUT`  | `/admin/payments/{id}/mark-as-failed`    | Mark payment as failed    | Admin Only   |

### 📊 Revenue Reports Endpoints (`/admin/reports/revenue`)

| Method | Endpoint | Description | Access Level |
|--------|----------|-------------|--------------|
| `GET` | `/admin/reports/revenue/check` | Check data availability | Admin Only |
| `GET` | `/admin/reports/revenue/daily` | Get daily revenue report | Admin Only |
| `GET` | `/admin/reports/revenue/monthly` | Get monthly revenue report | Admin Only |
| `GET` | `/admin/reports/revenue/yearly` | Get yearly revenue report | Admin Only |
| `GET` | `/admin/reports/revenue` | Get revenue report by period type | Admin Only |

**Revenue Report Parameters:**
- `startDate`: Start date (ISO format: YYYY-MM-DD)
- `endDate`: End date (ISO format: YYYY-MM-DD)
- `period`: Report period type (DAILY, MONTHLY, YEARLY) - for generic endpoint only

**Date Range Restrictions:**
- Maximum date range: 2 years
- Start date must be before or equal to end date
- Both dates are required

## 🏗️ Project Structure

```
src/
├── main/
│   ├── java/project/hotel_booking_system/
│   │   ├── 🔧 configuration/    # System configurations
│   │   ├── 🎮 controller/       # REST API endpoints 
│   │   ├── 📝 dto/             # Data Transfer Objects
│   │   ├── 🗂️ model/           # Entity models
│   │   ├── ⚠️ exception/        # Custom exceptions
│   │   ├── 📊 enums/           # Enumerations
│   │   ├── 🔄 mapper/          # Object mappers
│   │   ├── 🗄️ repository/      # Data repositories
│   │   ├── 🔧 service/         # Business logic
│   │   └── 🛡️ security/        # Security configurations
│   └── resources/
│       ├── application.yml      # App configuration
│       ├── static/             # Static resources
│       └── templates/          # Template files
```

## 🔐 Security Configuration

### Access Control Levels

#### 🌍 Public Endpoints (No Authentication Required)
- User registration
- Login
- Room search and information
- Room availability checking
- Review browsing
- Review summaries

#### 🔒 Protected Endpoints (JWT Required)
- Booking management
- Payment processing
- User profile management
- Creating and managing reviews

#### 👑 Admin Endpoints (ADMIN Role Required)
- User management
- Room management
- Payment management
- Room availability blocking/unblocking
- Review moderation
- Revenue reports
- System configuration


## 💳 VNPay Integration

### Configuration
```yml
vnpay:
  version: "2.1.0"
  command: "pay"
  locale: "vn"
  currCode: "VND"
  orderType: "billpayment"
```

### Payment Flow
1. 🛒 **Customer initiates payment**
2. 🔗 **System generates VNPay URL**
3. 💳 **Customer completes payment on VNPay**
4. 🔄 **VNPay sends callback to system**
5. ✅ **System validates and processes result**

## 🔧 Default Configuration

### Admin Account
- **Username**: `admin`
- **Password**: `admin`

### Application Settings
- **Base URL**: `http://localhost:8080/hotelbooking`
- **Default Port**: `8080`
- **Database**: `hotel_booking`

