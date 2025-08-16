package wisoft.nextframe.schedulereservationticketing.controller.performance;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformanceGenre;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformancePricing;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformancePricingId;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformanceType;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.Stadium;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.StadiumSection;
import wisoft.nextframe.schedulereservationticketing.repository.performance.PerformancePricingRepository;
import wisoft.nextframe.schedulereservationticketing.repository.performance.PerformanceRepository;
import wisoft.nextframe.schedulereservationticketing.repository.schedule.ScheduleRepository;
import wisoft.nextframe.schedulereservationticketing.repository.stadium.StadiumRepository;
import wisoft.nextframe.schedulereservationticketing.repository.stadium.StadiumSectionRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PerformanceControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private PerformanceRepository performanceRepository;
	@Autowired
	private StadiumRepository stadiumRepository;
	@Autowired
	private StadiumSectionRepository stadiumSectionRepository;
	@Autowired
	private ScheduleRepository scheduleRepository;
	@Autowired
	private PerformancePricingRepository performancePricingRepository;

	@Test
	@DisplayName("공연 상세 조회 통합 테스트 - 성공 (200 OK)")
	void getPerformanceDetail_Success() throws Exception {
		// given
		final Performance performance = createAndSaveTestData();
		final UUID performanceId = performance.getId();

		// when & then
		mockMvc.perform(get("/api/v1/performances/{id}", performanceId)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.data.id").value(performanceId.toString()))
			.andExpect(jsonPath("$.data.name").value("오페라의 유령"))
			.andExpect(jsonPath("$.data.stadium.name").value("부산문화회관"));
	}

	@Test
	@DisplayName("공연 상세 조회 통합 테스트 - 실패 (존재하지 않는 ID, 404 NOT FOUND)")
	void getPerformanceDetail_NotFound() throws Exception {
		// given
		final UUID notExistId = UUID.randomUUID();

		// when & then
		mockMvc.perform(get("/api/v1/performances/{id}", notExistId)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("공연 목록 조회 통합 테스트 - 성공 (200 OK)")
	void getPerformances_Success() throws Exception {
		// given: 예매 가능/불가능한 공연 데이터 저장
		createListOfTestData();

		// when and then
		mockMvc.perform(get("/api/v1/performances")
			.param("page", "0")
			.param("size", "10")
			.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			// 페이지네이션 정보 검증(예매 가능한 공연 1개만 조회되어야 함)
			.andExpect(jsonPath("$.data.pagination.totalItems").value(1))
			.andExpect(jsonPath("$.data.pagination.totalPages").value(1))
			// 공연 목록 컨텐츠 검증
			.andExpect(jsonPath("$.data.performances", hasSize(1)))
			.andExpect(jsonPath("$.data.performances[0].name").value("햄릿"))
			// MIN/MAX 날짜가 정확히 계산되었는지 검증
			.andExpect(jsonPath("$.data.performances[0].startDate").value("2025-09-01"))
			.andExpect(jsonPath("$.data.performances[0].endDate").value("2025-09-30"));
	}

	private Performance createAndSaveTestData() {
		Stadium stadium = Stadium.builder()
			.id(UUID.randomUUID())
			.name("부산문화회관")
			.address("부산 남구")
			.build();
		stadiumRepository.save(stadium);

		StadiumSection section = StadiumSection.builder()
			.id(UUID.randomUUID())
			.stadium(stadium)
			.section("A")
			.build();
		stadiumSectionRepository.save(section);

		Performance performance = Performance.builder()
			.id(UUID.randomUUID())
			.name("오페라의 유령")
			.type(PerformanceType.클래식)
			.genre(PerformanceGenre.뮤지컬)
			.adultOnly(true)
			.runningTime(Duration.ofMinutes(150))
			.imageUrl("http://example.com/image.jpg")
			.description("전설적인 뮤지컬")
			.build();
		performanceRepository.save(performance);

		Schedule schedule = Schedule.builder()
			.id(UUID.randomUUID())
			.performance(performance)
			.stadium(stadium)
			.performanceDatetime(LocalDateTime.of(2025, 10, 1, 19, 30))
			.ticketOpenTime(LocalDateTime.of(2025, 9, 1, 14, 0))
			.ticketCloseTime(LocalDateTime.of(2025, 9, 30, 17, 0))
			.build();
		scheduleRepository.save(schedule);

		PerformancePricing pricing = PerformancePricing.builder()
			.id(new PerformancePricingId(performance.getId(), section.getId()))
			.performance(performance)
			.stadiumSection(section)
			.price(120000)
			.build();
		performancePricingRepository.save(pricing);

		return performance;
	}

	private void createListOfTestData() {
		LocalDateTime now = LocalDateTime.now();
		Stadium stadium = stadiumRepository.save(Stadium.builder().id(UUID.randomUUID()).name("대전예술의전당").address("대전 서구").build());

		// 시나리오 1: 예매 가능한 공연 (결과에 포함되어야 함)
		Performance reservablePerf = performanceRepository.save(createPerformance("햄릿"));
		scheduleRepository.save(createSchedule(reservablePerf, stadium, now.minusDays(10), now.plusDays(10), LocalDate.of(2025, 9, 1).atStartOfDay()));
		scheduleRepository.save(createSchedule(reservablePerf, stadium, now.minusDays(10), now.plusDays(10), LocalDate.of(2025, 9, 30).atStartOfDay()));

		// 시나리오 2: 예매 시작 전 공연 (결과에 포함되면 안 됨)
		Performance notYetOpenPerf = performanceRepository.save(createPerformance("캣츠"));
		scheduleRepository.save(createSchedule(notYetOpenPerf, stadium, now.plusDays(1), now.plusDays(20), LocalDate.of(2025, 10, 1).atStartOfDay()));

		// 시나리오 3: 예매 마감된 공연 (결과에 포함되면 안 됨)
		Performance closedPerf = performanceRepository.save(createPerformance("오페라의 유령 - 마감"));
		scheduleRepository.save(createSchedule(closedPerf, stadium, now.minusDays(20), now.minusDays(1), now.minusDays(1).plusHours(2)));
	}

	private Performance createPerformance(String name) {
		return Performance.builder()
			.id(UUID.randomUUID())
			.name(name)
			.type(PerformanceType.동요)
			.genre(PerformanceGenre.뮤지컬)
			.adultOnly(false)
			.build();
	}

	private Schedule createSchedule(Performance performance, Stadium stadium, LocalDateTime open, LocalDateTime close, LocalDateTime perfDate) {
		return Schedule.builder()
			.id(UUID.randomUUID())
			.performance(performance)
			.stadium(stadium)
			.ticketOpenTime(open)
			.ticketCloseTime(close)
			.performanceDatetime(perfDate)
			.build();
	}
}
