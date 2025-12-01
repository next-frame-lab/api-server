package wisoft.nextframe.schedulereservationticketing.controller.reservation;

import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import wisoft.nextframe.schedulereservationticketing.common.exception.DomainException;
import wisoft.nextframe.schedulereservationticketing.common.exception.ErrorCode;
import wisoft.nextframe.schedulereservationticketing.config.jwt.JwtAuthenticationFilter;
import wisoft.nextframe.schedulereservationticketing.config.security.SecurityConfig;
import wisoft.nextframe.schedulereservationticketing.dto.reservation.request.ReservationRequest;
import wisoft.nextframe.schedulereservationticketing.dto.reservation.response.ReservationPerformanceResponse;
import wisoft.nextframe.schedulereservationticketing.dto.reservation.response.ReservationResponse;
import wisoft.nextframe.schedulereservationticketing.dto.reservation.response.ReservationSeatResponse;
import wisoft.nextframe.schedulereservationticketing.service.reservation.ReservationService;

@WebMvcTest(value = ReservationController.class,
	excludeFilters = @ComponentScan.Filter(
		type = FilterType.ASSIGNABLE_TYPE,
		classes = {SecurityConfig.class, JwtAuthenticationFilter.class}
	)
)
class ReservationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private ReservationService reservationService;

	private final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private final UUID PERFORMANCE_ID = UUID.randomUUID();
	private final UUID SCHEDULE_ID = UUID.randomUUID();

	@BeforeEach
	void setUp() {
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
			USER_ID,
			null,
			Collections.emptyList()
		);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	@Test
	@DisplayName("공연 좌석 예매 성공: 201 Created와 예약 정보를 반환한다")
	void reserveSeat_success() throws Exception {
		// given
		UUID seatId1 = UUID.randomUUID();
		UUID seatId2 = UUID.randomUUID();

		ReservationRequest request = new ReservationRequest(
			PERFORMANCE_ID,
			SCHEDULE_ID,
			List.of(seatId1, seatId2),
			500L,
			300000
		);

		ReservationPerformanceResponse performanceResponse = new ReservationPerformanceResponse(
			"햄릿",
			LocalDate.of(2025, 7, 15),
			LocalTime.of(19, 30)
		);

		List<ReservationSeatResponse> seatResponses = List.of(
			new ReservationSeatResponse("A", 1, 12),
			new ReservationSeatResponse("A", 1, 13)
		);

		ReservationResponse response = ReservationResponse.builder()
			.reservationId(UUID.randomUUID())
			.performance(performanceResponse)
			.seats(seatResponses)
			.totalAmount(300000)
			.build();

		given(reservationService.reserveSeat(eq(USER_ID), any(ReservationRequest.class)))
			.willReturn(response);

		// when and then
		mockMvc.perform(post("/api/v1/reservations")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.data.reservationId").exists())
			// Performance 검증
			.andExpect(jsonPath("$.data.performance.name").value("햄릿"))
			.andExpect(jsonPath("$.data.performance.scheduleDate").value("2025-07-15"))
			// Seats 검증
			.andExpect(jsonPath("$.data.seats").isArray())
			.andExpect(jsonPath("$.data.seats[0].section").value("A"))
			.andExpect(jsonPath("$.data.seats[0].row").value(1))
			.andExpect(jsonPath("$.data.seats[0].column").value(12))
			.andExpect(jsonPath("$.data.totalAmount").value(300000));
	}

	@Test
	@DisplayName("예약 실패: 좌석 ID 목록이 비어있으면 400 에러를 반환한다")
	void reserveSeat_fail_empty_seats() throws Exception {
		// given
		ReservationRequest request = new ReservationRequest(
			PERFORMANCE_ID,
			SCHEDULE_ID,
			List.of(), // Empty List (@NotEmpty 위반)
			100L,
			150000
		);

		// when and then
		mockMvc.perform(post("/api/v1/reservations")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("BAD_REQUEST"));
	}

	@Test
	@DisplayName("예약 실패: 이미 선점된 좌석인 경우 409 Conflict를 반환한다")
	void reserveSeat_fail_already_locked() throws Exception {
		// given
		ReservationRequest request = new ReservationRequest(
			PERFORMANCE_ID,
			SCHEDULE_ID,
			List.of(UUID.randomUUID()),
			100L,
			150000
		);

		given(reservationService.reserveSeat(eq(USER_ID), any(ReservationRequest.class)))
			.willThrow(new DomainException(ErrorCode.SEAT_ALREADY_LOCKED));

		// when and then
		mockMvc.perform(post("/api/v1/reservations")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("CONFLICT"));
	}

	@Test
	@DisplayName("예약 실패: 결제 금액이 불일치하는 경우 400 Bad Request를 반환한다")
	void reserveSeat_fail_price_mismatch() throws Exception {
		// given
		ReservationRequest request = new ReservationRequest(
			PERFORMANCE_ID,
			SCHEDULE_ID,
			List.of(UUID.randomUUID()),
			100L,
			500 // Wrong Amount
		);

		given(reservationService.reserveSeat(eq(USER_ID), any(ReservationRequest.class)))
			.willThrow(new DomainException(ErrorCode.TOTAL_PRICE_MISMATCH));

		// when and then
		mockMvc.perform(post("/api/v1/reservations")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("BAD_REQUEST"));
	}
}