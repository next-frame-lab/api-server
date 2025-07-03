package wisoft.nextframe.schedulereservationticketing.controller.stadium;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.common.response.ApiResponse;
import wisoft.nextframe.schedulereservationticketing.dto.seat.seatdefinition.SeatDefinitionListResponse;
import wisoft.nextframe.schedulereservationticketing.service.seat.SeatDefinitionService;

@RestController
@RequestMapping("/api/v1/stadiums")
@RequiredArgsConstructor
public class StadiumController {

	private final SeatDefinitionService seatDefinitionService;

	@GetMapping("/{id}/seat-definitions")
	public ResponseEntity<ApiResponse<?>> getSeatDefinitions(@PathVariable UUID id) {
		final SeatDefinitionListResponse data = seatDefinitionService.getSeatDefinitions(id);

		final ApiResponse<SeatDefinitionListResponse> response = ApiResponse.success(data);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
