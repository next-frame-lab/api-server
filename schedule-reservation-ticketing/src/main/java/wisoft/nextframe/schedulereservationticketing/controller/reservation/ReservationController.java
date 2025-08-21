package wisoft.nextframe.schedulereservationticketing.controller.reservation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.common.response.ApiResponse;
import wisoft.nextframe.schedulereservationticketing.dto.reservation.request.ReservationRequest;
import wisoft.nextframe.schedulereservationticketing.dto.reservation.response.ReservationResponse;
import wisoft.nextframe.schedulereservationticketing.service.reservation.ReservationService;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

	private final ReservationService reservationService;

	@PostMapping
	public ResponseEntity<ApiResponse<?>> reserveSeat(@Valid @RequestBody ReservationRequest request) {
		final ReservationResponse reservationResponse = reservationService.reserveSeat(request);
		final ApiResponse<ReservationResponse> response = ApiResponse.success(reservationResponse);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}
