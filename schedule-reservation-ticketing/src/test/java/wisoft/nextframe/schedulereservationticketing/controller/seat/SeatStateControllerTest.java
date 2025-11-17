package wisoft.nextframe.schedulereservationticketing.controller.seat;

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

import wisoft.nextframe.schedulereservationticketing.common.exception.DomainException;
import wisoft.nextframe.schedulereservationticketing.common.exception.ErrorCode;
import wisoft.nextframe.schedulereservationticketing.config.jwt.JwtAuthenticationFilter;
import wisoft.nextframe.schedulereservationticketing.config.security.SecurityConfig;
import wisoft.nextframe.schedulereservationticketing.dto.seat.seatstate.SeatStateListResponse;
import wisoft.nextframe.schedulereservationticketing.dto.seat.seatstate.SeatStateResponse;
import wisoft.nextframe.schedulereservationticketing.service.seat.SeatStateFacade;

@WebMvcTest(value = SeatStateController.class,
	excludeFilters = @ComponentScan.Filter(
		type = FilterType.ASSIGNABLE_TYPE,
		classes = {SecurityConfig.class, JwtAuthenticationFilter.class}
	)
)
@WithMockUser
class SeatStateControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private SeatStateFacade seatStateFacade;

	@Test
	@DisplayName("좌석 상태 조회 성공: 200 OK와 좌석 리스트를 반환한다")
	void getLockedSeats_success() throws Exception {
		// given
		UUID scheduleId = UUID.randomUUID();
		UUID seatId1 = UUID.randomUUID();
		UUID seatId2 = UUID.randomUUID();

		SeatStateResponse seat1 = new SeatStateResponse(seatId1, true);
		SeatStateResponse seat2 = new SeatStateResponse(seatId2, true);

		SeatStateListResponse response = new SeatStateListResponse(List.of(seat1, seat2));

		given(seatStateFacade.getSeatStates(scheduleId))
			.willReturn(response);

		// when and then
		mockMvc.perform(get("/api/v1/schedules/{scheduleId}/seat-states", scheduleId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			// List 구조 검증
			.andExpect(jsonPath("$.data.seats").isArray())
			.andExpect(jsonPath("$.data.seats.length()").value(2))
			// 첫 번째 좌석 값 검증
			.andExpect(jsonPath("$.data.seats[0].id").value(seatId1.toString()))
			.andExpect(jsonPath("$.data.seats[0].isLocked").value(true))
			// 두 번째 좌석 값 검증
			.andExpect(jsonPath("$.data.seats[1].id").value(seatId2.toString()))
			.andExpect(jsonPath("$.data.seats[1].isLocked").value(true));
	}

	@Test
	@DisplayName("좌석 상태 조회 실패: 잘못된 UUID 형식이 입력되면 400 Bad Request를 반환한다")
	void getLockedSeats_fail_invalid_uuid() throws Exception {
		// given
		String invalidScheduleId = "invalid-uuid-format";

		// when and then
		mockMvc.perform(get("/api/v1/schedules/{scheduleId}/seat-states", invalidScheduleId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest())
		  .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
	}

	@Test
	@DisplayName("좌석 상태 조회 실패: 존재하지 않는 스케줄 ID면 404 Not Found를 반환한다")
	void getLockedSeats_fail_not_found() throws Exception {
		// given
		UUID notFoundScheduleId = UUID.randomUUID();

		given(seatStateFacade.getSeatStates(notFoundScheduleId))
			.willThrow(new DomainException(ErrorCode.SCHEDULE_NOT_FOUND));

		// when and then
		mockMvc.perform(get("/api/v1/schedules/{scheduleId}/seat-states", notFoundScheduleId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound()) // 404 확인
			.andExpect(jsonPath("$.code").value("NOT_FOUND"));
	}
}