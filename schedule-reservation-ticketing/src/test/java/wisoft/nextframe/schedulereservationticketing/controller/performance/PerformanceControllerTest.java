package wisoft.nextframe.schedulereservationticketing.controller.performance;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

import wisoft.nextframe.schedulereservationticketing.builder.PerformanceBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.PerformancePricingBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.ScheduleBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.StadiumBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.StadiumSectionBuilder;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
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
		final Stadium stadium = stadiumRepository.save(new StadiumBuilder().withName("부산문화회관").build());
		final StadiumSection section = stadiumSectionRepository.save(
			new StadiumSectionBuilder().withStadium(stadium).build());
		final Performance performance = performanceRepository.save(new PerformanceBuilder().withName("오페라의 유령").build());
		scheduleRepository.save(new ScheduleBuilder().withPerformance(performance).withStadium(stadium).build());
		performancePricingRepository.save(new PerformancePricingBuilder().withPerformance(performance).withStadiumSection(section).build());
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
		// given
		LocalDateTime now = LocalDateTime.now();
		Stadium stadium = stadiumRepository.save(new StadiumBuilder().withName("대전예술의전당").build());

		// 시나리오 1: 예매 가능한 공연 (결과에 포함되어야 함)
		Performance reservablePerf = performanceRepository.save(new PerformanceBuilder().withName("햄릿").build());
		scheduleRepository.save(new ScheduleBuilder()
			.withPerformance(reservablePerf).withStadium(stadium)
			.withTicketOpenTime(now.minusDays(10)).withTicketCloseTime(now.plusDays(10))
			.withPerformanceDatetime(LocalDate.of(2025, 9, 1).atStartOfDay()).build());
		scheduleRepository.save(new ScheduleBuilder()
			.withPerformance(reservablePerf).withStadium(stadium)
			.withTicketOpenTime(now.minusDays(10)).withTicketCloseTime(now.plusDays(10))
			.withPerformanceDatetime(LocalDate.of(2025, 9, 30).atStartOfDay()).build());

		// 시나리오 2: 예매 시작 전 공연 (결과에 포함되면 안 됨)
		Performance notYetOpenPerf = performanceRepository.save(new PerformanceBuilder().withName("캣츠").build());
		scheduleRepository.save(new ScheduleBuilder()
			.withPerformance(notYetOpenPerf).withStadium(stadium)
			.withTicketOpenTime(now.plusDays(1)).withTicketCloseTime(now.plusDays(20)).build());

		// 시나리오 3: 예매 마감된 공연 (결과에 포함되면 안 됨)
		Performance closedPerf = performanceRepository.save(new PerformanceBuilder().withName("오페라의 유령").build());
		scheduleRepository.save(new ScheduleBuilder()
			.withPerformance(closedPerf).withStadium(stadium)
			.withTicketOpenTime(now.minusDays(20)).withTicketCloseTime(now.minusDays(1)).build());

		// when and then
		mockMvc.perform(get("/api/v1/performances")
				.param("page", "0").param("size", "10")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.performances", hasSize(1)))
			.andExpect(jsonPath("$.data.performances[0].name").value("햄릿"))
			.andExpect(jsonPath("$.data.performances[0].startDate").value("2025-09-01"))
			.andExpect(jsonPath("$.data.performances[0].endDate").value("2025-09-30"));
	}
}
