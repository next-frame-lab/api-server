package wisoft.nextframe.schedulereservationticketing.controller.stadium;

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
import wisoft.nextframe.schedulereservationticketing.dto.seat.seatdefinition.SeatDefinitionListResponse;
import wisoft.nextframe.schedulereservationticketing.service.seat.SeatDefinitionService;

@Slf4j
@RestController
@RequestMapping("/api/v1/stadiums")
@RequiredArgsConstructor
public class StadiumController {

	private final SeatDefinitionService seatDefinitionService;

	@GetMapping("/{id}/seat-definitions")
	public ResponseEntity<ApiResponse<?>> getSeatDefinitions(@PathVariable UUID id) {
		log.info("공연장 좌석 정보 조회 요청. stadiumId: {}", id);
		final SeatDefinitionListResponse data = seatDefinitionService.getSeatDefinitions(id);
		log.info("공연장 좌석 정보 조회 완료. stadiumId: {}, seatCount: {}", id, data.seats().size());

		final ApiResponse<SeatDefinitionListResponse> response = ApiResponse.success(data);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
