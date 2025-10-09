package wisoft.nextframe.schedulereservationticketing.controller.reservation;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.schedulereservationticketing.common.response.ApiResponse;
import wisoft.nextframe.schedulereservationticketing.dto.reservation.request.ReservationRequest;
import wisoft.nextframe.schedulereservationticketing.dto.reservation.response.ReservationResponse;
import wisoft.nextframe.schedulereservationticketing.service.reservation.ReservationService;

@Slf4j
@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

	private final ReservationService reservationService;

	@PostMapping
	public ResponseEntity<ApiResponse<?>> reserveSeat(
		@AuthenticationPrincipal UUID userId,
		@Valid @RequestBody ReservationRequest request
	) {
		final ReservationResponse reservationResponse = reservationService.reserveSeat(userId, request);

		final ApiResponse<ReservationResponse> response = ApiResponse.success(reservationResponse);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}
