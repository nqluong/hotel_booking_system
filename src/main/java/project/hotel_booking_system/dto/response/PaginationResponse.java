package project.hotel_booking_system.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaginationResponse<T> {
    List<T> content; // Danh sách kết quả
    int currentPage; // Trang hiện tại
    int totalPages; // Tổng số trang
    long totalElements; // Tổng số phần tử
    int pageSize; // Kích thước mỗi trang

}

