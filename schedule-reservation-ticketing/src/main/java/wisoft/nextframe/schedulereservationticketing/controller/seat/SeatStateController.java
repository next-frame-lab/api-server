package wisoft.nextframe.schedulereservationticketing.controller.seat;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.schedulereservationticketing.common.response.ApiResponse;
import wisoft.nextframe.schedulereservationticketing.dto.seat.seatstate.SeatStateListResponse;
import wisoft.nextframe.schedulereservationticketing.service.seat.SeatStateService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/schedules")
public class SeatStateController {

	private final SeatStateService seatStateService;

	@GetMapping("/{scheduleId}/seat-states")
	public ResponseEntity<ApiResponse<?>> getLockedSeats(@PathVariable UUID scheduleId) {
		final SeatStateListResponse data = seatStateService.getSeatStates(scheduleId);

		final ApiResponse<SeatStateListResponse> response = ApiResponse.success(data);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
}
