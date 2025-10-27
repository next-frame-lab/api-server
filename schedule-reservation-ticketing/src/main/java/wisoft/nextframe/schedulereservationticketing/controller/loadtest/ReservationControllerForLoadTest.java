package wisoft.nextframe.schedulereservationticketing.controller.loadtest;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.common.response.ApiResponse;
import wisoft.nextframe.schedulereservationticketing.dto.reservation.request.ReservationRequest;
import wisoft.nextframe.schedulereservationticketing.dto.reservation.response.ReservationResponse;
import wisoft.nextframe.schedulereservationticketing.service.reservation.ReservationService;

@Profile("loadtest")
@RestController
@RequestMapping("/api/v1/loadtest/reservations")
@RequiredArgsConstructor
public class ReservationControllerForLoadTest {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> reserveSeat(
        @RequestHeader("X-USER-ID") UUID userId,  // loadtest 환경에서는 헤더에서 받음
        @Valid @RequestBody ReservationRequest request
    ) {
        final ReservationResponse reservationResponse = reservationService.reserveSeat(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(reservationResponse));
    }
}
