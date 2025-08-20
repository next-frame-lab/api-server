package wisoft.nextframe.schedulereservationticketing.dto.reservation.request;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReservationRequest {


	@NotNull
	private final UUID userId;

	@NotNull
	private final UUID performanceId;

	@NotNull
	private final UUID scheduleId;

	@NotEmpty
	@Size(max = 4)
	private final List<UUID> seatIds;

	private final long elapsedTime;

	private final int totalAmount;
}
