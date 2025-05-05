# Hotel Booking System Admin API

## Admin Reservation Management API

This API enables administrators to view and manage hotel reservations, including confirming bookings, checking in guests, and checking out guests.

### Endpoints

#### View All Bookings
```
GET /api/admin/bookings
```
Returns a list of all bookings in the system.

#### Get Booking by ID
```
GET /api/admin/bookings/{id}
```
Returns detailed information about a specific booking.

#### Update Booking Status
```
PUT /api/admin/bookings/{id}/status
```
Updates the status of a booking. Request body:
```json
{
  "status": "CONFIRMED"
}
```
Possible status values: `PENDING`, `CONFIRMED`, `CHECKED_IN`, `COMPLETED`, `CANCELLED`

#### Filter Bookings by Status
```
GET /api/admin/bookings/status/{status}
```
Returns all bookings with the specified status.
Status values: `PENDING`, `CONFIRMED`, `CHECKED_IN`, `COMPLETED`, `CANCELLED`

#### Confirm Booking
```
PUT /api/admin/bookings/{id}/confirm
```
Updates a booking status to `CONFIRMED`.

#### Check-in Guest
```
PUT /api/admin/bookings/{id}/check-in
```
Updates a booking status to `CHECKED_IN`.

#### Check-out Guest
```
PUT /api/admin/bookings/{id}/check-out
```
Updates a booking status to `COMPLETED`.

#### Cancel Booking
```
PUT /api/admin/bookings/{id}/cancel
```
Updates a booking status to `CANCELLED`.

### Status Transition Rules

The API enforces the following status transition rules:
- `PENDING` → `CONFIRMED` or `CANCELLED`
- `CONFIRMED` → `CHECKED_IN` or `CANCELLED`
- `CHECKED_IN` → `COMPLETED`
- `COMPLETED` → No further transitions allowed
- `CANCELLED` → No further transitions allowed

### Response Format

```json
{
  "id": 1,
  "userId": 101,
  "userName": "John Doe",
  "roomId": 201,
  "roomNumber": "101A",
  "checkInDate": "2023-12-20T15:00:00",
  "checkOutDate": "2023-12-22T11:00:00",
  "status": "CONFIRMED",
  "totalPrice": 250.00,
  "createdAt": "2023-12-15T09:30:45"
}
```

## Implementation Notes

To complete the implementation, the following requirements must be met:

1. Ensure that the Booking model has setter methods or uses Lombok's @Setter annotation
2. Update BookingRepository with a findByStatus method
3. Make sure User and Room entities have appropriate getter methods
4. Fix any remaining linter errors in the service implementation

## Security Considerations

The admin API should be protected with appropriate authentication and authorization mechanisms. The current implementation will need to be further secured before production deployment. 