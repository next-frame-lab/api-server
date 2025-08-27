package wisoft.nextframe.schedulereservationticketing.dto.reservation.request;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;

public record ReservationRequest(@NotNull UUID userId, @NotNull UUID performanceId, @NotNull UUID scheduleId,
																 @NotEmpty @Size(max = 4) List<UUID> seatIds, long elapsedTime, int totalAmount) {
}
