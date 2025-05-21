package project.hotel_booking_system.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class PaginationResponse<T> {
     List<T> content;
     int page;
     int pageSize;
     long totalElements;
     int totalPages;
     boolean last;
}

