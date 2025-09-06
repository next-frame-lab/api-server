package wisoft.nextframe.schedulereservationticketing.controller.performance;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import wisoft.nextframe.schedulereservationticketing.builder.PerformanceBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.PerformancePricingBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.ScheduleBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.StadiumBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.StadiumSectionBuilder;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
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
	@WithMockUser
	void getPerformanceDetail_Success() throws Exception {
		// given
		final Stadium stadium = stadiumRepository.save(new StadiumBuilder().withName("부산문화회관").build());
		final StadiumSection section = stadiumSectionRepository.save(
			new StadiumSectionBuilder().withStadium(stadium).build());
		final Performance performance = performanceRepository.save(new PerformanceBuilder().withName("오페라의 유령").build());
		final Schedule schedule = scheduleRepository.save(
			new ScheduleBuilder().withPerformance(performance).withStadium(stadium).build());
		performancePricingRepository.save(new PerformancePricingBuilder().withSchedule(schedule).withStadiumSection(section).build());
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
	@WithMockUser
	void getPerformanceDetail_NotFound() throws Exception {
		// given
		final UUID notExistId = UUID.randomUUID();

		// when & then
		mockMvc.perform(get("/api/v1/performances/{id}", notExistId)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound());
	}

	// todo
	// @Test
	// @DisplayName("공연 목록 조회 통합 테스트 - 성공 (200 OK)")
	// void getPerformances_Success() throws Exception {
	// 	// given
	// 	final LocalDateTime now = LocalDateTime.now();
	// 	final Stadium stadium = stadiumRepository.save(new StadiumBuilder().build());
	//
	// 	// 1. 이 테스트만을 위한 고유 식별자 생성
	// 	final String uniqueId = UUID.randomUUID().toString();
	//
	// 	// 시나리오 1: 예매 가능한 공연 (결과에 포함되어야 함)
	// 	final Performance reservablePerf = performanceRepository.save(
	// 		new PerformanceBuilder().withName("햄릿-" + uniqueId).build()
	// 	);
	// 	final UUID expectedPerfId = reservablePerf.getId(); // 검증에 사용할 고유 ID 저장
	//
	// 	scheduleRepository.save(new ScheduleBuilder()
	// 		.withPerformance(reservablePerf).withStadium(stadium)
	// 		.withTicketOpenTime(now.minusDays(10)).withTicketCloseTime(now.plusDays(10))
	// 		.withPerformanceDatetime(LocalDate.of(2025, 9, 1).atStartOfDay()).build());
	// 	scheduleRepository.save(new ScheduleBuilder()
	// 		.withPerformance(reservablePerf).withStadium(stadium)
	// 		.withTicketOpenTime(now.minusDays(10)).withTicketCloseTime(now.plusDays(10))
	// 		.withPerformanceDatetime(LocalDate.of(2025, 9, 30).atStartOfDay()).build());
	//
	// 	// 시나리오 2, 3: 조회되지 않아야 할 공연들
	// 	final Performance notYetOpenPerf = performanceRepository.save(
	// 		new PerformanceBuilder().withName("캣츠-" + uniqueId).build()
	// 	);
	// 	scheduleRepository.save(new ScheduleBuilder()
	// 		.withPerformance(notYetOpenPerf).withStadium(stadium)
	// 		.withTicketOpenTime(now.plusDays(1)).withTicketCloseTime(now.plusDays(20)).build());
	//
	// 	final Performance closedPerf = performanceRepository.save(
	// 		new PerformanceBuilder().withName("오페라의 유령-" + uniqueId).build()
	// 	);
	// 	scheduleRepository.save(new ScheduleBuilder()
	// 		.withPerformance(closedPerf).withStadium(stadium)
	// 		.withTicketOpenTime(now.minusDays(20)).withTicketCloseTime(now.minusDays(1)).build());
	//
	// 	// when
	// 	MvcResult mvcResult = mockMvc.perform(get("/api/v1/performances")
	// 			.param("page", "0").param("size", "10")
	// 			.accept(MediaType.APPLICATION_JSON))
	// 		.andExpect(status().isOk())
	// 		.andReturn(); // 응답 결과를 MvcResult 객체로 받음
	//
	// 	// then
	// 	// 2. 응답 JSON을 DTO 객체로 파싱
	// 	String jsonResponse = mvcResult.getResponse().getContentAsString();
	// 	ApiResponse<PerformanceListResponse> response = objectMapper.readValue(jsonResponse, new TypeReference<>() {});
	// 	List<PerformanceResponse> performances = response.getData().getPerformances();
	//
	// 	// 3. 전체 결과 리스트에서 내가 만든 '예매 가능한' 공연만 필터링
	// 	List<PerformanceResponse> foundSpecificPerf = performances.stream()
	// 		.filter(p -> p.getId().equals(expectedPerfId))
	// 		.toList();
	//
	// 	// 4. 필터링된 결과가 정확히 1개인지, 그리고 그 내용이 올바른지 검증
	// 	assertThat(foundSpecificPerf).hasSize(1);
	//
	// 	PerformanceResponse summaryDto = foundSpecificPerf.getFirst();
	// 	assertThat(summaryDto.getName()).isEqualTo("햄릿-" + uniqueId);
	// 	assertThat(summaryDto.getStartDate()).isEqualTo(LocalDate.of(2025, 9, 1));
	// 	assertThat(summaryDto.getEndDate()).isEqualTo(LocalDate.of(2025, 9, 30));
	// }
}
