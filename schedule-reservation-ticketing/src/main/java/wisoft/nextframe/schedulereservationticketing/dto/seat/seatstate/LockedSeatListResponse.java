package wisoft.nextframe.schedulereservationticketing.dto.seat.seatstate;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
public record LockedSeatListResponse(List<LockedSeatResponse> seats) {
}
