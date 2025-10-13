package wisoft.nextframe.schedulereservationticketing.controller.review;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import wisoft.nextframe.schedulereservationticketing.builder.PerformanceBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.ScheduleBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.StadiumBuilder;
import wisoft.nextframe.schedulereservationticketing.config.AbstractIntegrationTest;
import wisoft.nextframe.schedulereservationticketing.dto.review.ReviewCreateRequest;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.reservation.Reservation;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.Stadium;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;
import wisoft.nextframe.schedulereservationticketing.repository.performance.PerformanceRepository;
import wisoft.nextframe.schedulereservationticketing.repository.reservation.ReservationRepository;
import wisoft.nextframe.schedulereservationticketing.repository.schedule.ScheduleRepository;
import wisoft.nextframe.schedulereservationticketing.repository.stadium.StadiumRepository;
import wisoft.nextframe.schedulereservationticketing.repository.user.UserRepository;

@AutoConfigureMockMvc
class ReviewControllerTest extends AbstractIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PerformanceRepository performanceRepository;
	@Autowired
	private StadiumRepository stadiumRepository;
	@Autowired
	private ScheduleRepository scheduleRepository;
	@Autowired
	private ReservationRepository reservationRepository;

	@Test
	@DisplayName("리뷰 생성 통합 테스트 - 성공 (201 CREATED)")
	void createReview_Success() throws Exception {
		// given: 사용자, 공연, 일정, 예매 데이터 준비
		User user = userRepository.save(User.builder().name("홍길동").build());
		Stadium stadium = stadiumRepository.save(new StadiumBuilder().withName("대전예술의전당").build());
		Performance performance = performanceRepository.save(new PerformanceBuilder().withName("햄릿").build());
		Schedule schedule = scheduleRepository.save(new ScheduleBuilder().withPerformance(performance).withStadium(stadium).build());
		reservationRepository.save(Reservation.create(user, schedule, 10000));

		UUID performanceId = performance.getId();

		ReviewCreateRequest request = new ReviewCreateRequest(BigDecimal.valueOf(4.5), "정말 멋진 공연!");
		String json = objectMapper.writeValueAsString(request);

		// AuthenticationPrincipal에 UUID를 넣기 위해 principal을 UUID로 구성
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user.getId(), null);

		// when & then
		mockMvc.perform(post("/api/v1/performances/{performanceId}/reviews", performanceId)
				.with(authentication(auth))
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.data.id", notNullValue()));
	}

	@Test
	@DisplayName("리뷰 생성 통합 테스트 - 실패 (예매 내역 없음, 403 FORBIDDEN)")
	void createReview_Forbidden_WhenNoReservation() throws Exception {
		// given: 사용자, 공연, 일정만 존재 (예매 없음)
		User user = userRepository.save(User.builder().name("김철수").build());
		Stadium stadium = stadiumRepository.save(new StadiumBuilder().withName("부산문화회관").build());
		Performance performance = performanceRepository.save(new PerformanceBuilder().withName("오페라의 유령").build());
		Schedule schedule = scheduleRepository.save(new ScheduleBuilder().withPerformance(performance).withStadium(stadium).build());

		UUID performanceId = performance.getId();

		ReviewCreateRequest request = new ReviewCreateRequest(BigDecimal.valueOf(3.0), "재밌었어요");
		String json = objectMapper.writeValueAsString(request);

		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user.getId(), null);

		// when & then
		mockMvc.perform(post("/api/v1/performances/{performanceId}/reviews", performanceId)
				.with(authentication(auth))
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("FORBIDDEN"));
	}
}
