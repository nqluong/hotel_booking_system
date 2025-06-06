# 🏨 Hotel Booking System

A comprehensive hotel booking management system built with Spring Boot, providing robust APIs for room reservations, user authentication, and payment processing.

## 📋 Overview

This application is a modern hotel booking management system featuring high security, online payment integration, and role-based user management designed for both customers and hotel administrators.

## ✨ Key Features

- 🔐 **User Authentication & Authorization** - Secure JWT-based authentication
- 🏠 **Room Booking Management** - Complete reservation lifecycle management
- 💳 **VNPay Payment Integration** - Secure online payment processing
- 👥 **Role-based Access Control** - Admin and Customer role separation
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
git clone https://github.com/your-username/hotel-booking-system.git
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
```yml
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

### 🔑 Authentication Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/auth/token` | User login |
| `POST` | `/auth/refresh` | Refresh access token |
| `POST` | `/auth/logout` | User logout |
| `POST` | `/auth/introspect` | Validate token |

### 💳 Payment Endpoints

#### Customer Payment APIs (`/payments`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/payments/process-payment` | Process VNPay payment |
| `POST` | `/payments/process-checkout-payment` | Process checkout payment |
| `POST` | `/payments/cash` | Process cash payment |
| `GET` | `/payments/{id}` | Get payment details |
| `GET` | `/payments/booking/{bookingId}` | Get booking payments |
| `GET` | `/payments/vnpay-callback` | VNPay payment callback |

#### Admin Payment Management (`/admin/payments`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/admin/payments` | Get all payments |
| `GET` | `/admin/payments/{id}` | Get payment by ID |
| `GET` | `/admin/payments/status/{status}` | Get payments by status |
| `PUT` | `/admin/payments/{id}/status` | Update payment status |
| `PUT` | `/admin/payments/{id}/mark-as-completed` | Mark as completed |
| `PUT` | `/admin/payments/{id}/mark-as-failed` | Mark as failed |

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
- Room search
- Basic room information

#### 🔒 Protected Endpoints (JWT Required)
- Booking management
- Payment processing
- User profile management

#### 👑 Admin Endpoints (ADMIN Role Required)
- Room and image management
- Booking management
- Payment management
- User management
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

