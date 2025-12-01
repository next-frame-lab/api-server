package wisoft.nextframe.schedulereservationticketing.controller.stadium;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import wisoft.nextframe.schedulereservationticketing.common.exception.DomainException;
import wisoft.nextframe.schedulereservationticketing.common.exception.ErrorCode;
import wisoft.nextframe.schedulereservationticketing.config.jwt.JwtAuthenticationFilter;
import wisoft.nextframe.schedulereservationticketing.config.security.SecurityConfig;
import wisoft.nextframe.schedulereservationticketing.dto.seat.seatdefinition.SeatDefinitionListResponse;
import wisoft.nextframe.schedulereservationticketing.dto.seat.seatdefinition.SeatDefinitionResponse;
import wisoft.nextframe.schedulereservationticketing.service.seat.SeatDefinitionService;

@WebMvcTest(value = StadiumController.class,
	excludeFilters = @ComponentScan.Filter(
		type = FilterType.ASSIGNABLE_TYPE,
		classes = {SecurityConfig.class, JwtAuthenticationFilter.class}
	)
)
@WithMockUser
class StadiumControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private SeatDefinitionService seatDefinitionService;

	@Test
	@DisplayName("공연장 좌석 조회 성공: 200 OK와 구역/행/열 정보를 포함한 리스트를 반환한다")
	void getSeatDefinitions_success() throws Exception {
		// given
		UUID stadiumId = UUID.randomUUID();
		UUID seatDefId1 = UUID.randomUUID();
		UUID seatDefId2 = UUID.randomUUID();

		SeatDefinitionResponse seat1 = new SeatDefinitionResponse(seatDefId1, "A", 1, 1);
		SeatDefinitionResponse seat2 = new SeatDefinitionResponse(seatDefId2, "A", 1, 2);

		SeatDefinitionListResponse response = new SeatDefinitionListResponse(List.of(seat1, seat2));

		given(seatDefinitionService.getSeatDefinitions(stadiumId))
			.willReturn(response);

		// when and then
		mockMvc.perform(get("/api/v1/stadiums/{id}/seat-definitions", stadiumId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			// List 구조 검증
			.andExpect(jsonPath("$.data.seats").isArray())
			.andExpect(jsonPath("$.data.seats.length()").value(2))
			// 첫 번째 좌석 데이터 검증
			.andExpect(jsonPath("$.data.seats[0].id").value(seatDefId1.toString()))
			.andExpect(jsonPath("$.data.seats[0].section").value("A"))
			.andExpect(jsonPath("$.data.seats[0].row").value(1))
			.andExpect(jsonPath("$.data.seats[0].column").value(1))
			// 두 번째 좌석 데이터 검증
			.andExpect(jsonPath("$.data.seats[1].id").value(seatDefId2.toString()))
			.andExpect(jsonPath("$.data.seats[1].column").value(2));
	}

	@Test
	@DisplayName("좌석 정의 조회 실패: 잘못된 UUID 형식이 입력되면 400 Bad Request를 반환한다")
	void getSeatDefinitions_fail_invalid_uuid() throws Exception {
		// given
		String invalidId = "invalid-stadium-id";

		// when and then
		mockMvc.perform(get("/api/v1/stadiums/{id}/seat-definitions", invalidId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("BAD_REQUEST"));
	}

	@Test
	@DisplayName("좌석 정의 조회 실패: 존재하지 않는 경기장 ID면 404 Not Found를 반환한다")
	void getSeatDefinitions_fail_not_found() throws Exception {
		// given
		UUID notFoundStadiumId = UUID.randomUUID();

		given(seatDefinitionService.getSeatDefinitions(notFoundStadiumId))
			.willThrow(new DomainException(ErrorCode.STADIUM_NOT_FOUND));

		// when and then
		mockMvc.perform(get("/api/v1/stadiums/{id}/seat-definitions", notFoundStadiumId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("NOT_FOUND"));
	}
}