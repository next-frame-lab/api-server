package wisoft.nextframe.schedulereservationticketing.controller.performance;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.Duration;
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
}
