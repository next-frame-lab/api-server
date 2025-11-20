package wisoft.nextframe.schedulereservationticketing.controller.performance;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import wisoft.nextframe.schedulereservationticketing.common.exception.DomainException;
import wisoft.nextframe.schedulereservationticketing.common.exception.ErrorCode;
import wisoft.nextframe.schedulereservationticketing.common.exception.GlobalExceptionHandler;
import wisoft.nextframe.schedulereservationticketing.dto.performance.performancedetail.response.PerformanceDetailResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performance.performancedetail.response.StadiumResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performance.performancelist.response.PaginationResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performance.performancelist.response.PerformanceListResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performance.performancelist.response.PerformanceSummaryResponse;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformanceGenre;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformanceType;
import wisoft.nextframe.schedulereservationticketing.service.auth.DynamicAuthService;
import wisoft.nextframe.schedulereservationticketing.service.performance.PerformanceService;

@WebMvcTest(value = PerformanceController.class,
	excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
		classes = {
			wisoft.nextframe.schedulereservationticketing.config.security.SecurityConfig.class,
			wisoft.nextframe.schedulereservationticketing.config.jwt.JwtAuthenticationFilter.class
		})
)
@Import(GlobalExceptionHandler.class)
class PerformanceControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PerformanceService performanceService;

	@Autowired
	private DynamicAuthService dynamicAuthService;

	@TestConfiguration
	static class TestConfig {
		@Bean
		PerformanceService performanceService() {
			return Mockito.mock(PerformanceService.class);
		}

		@Bean(name = "dynamicAuthService")
		DynamicAuthService dynamicAuthService() {
			return Mockito.mock(DynamicAuthService.class);
		}
	}

	@BeforeEach
	void setUp() {
		Mockito.reset(performanceService, dynamicAuthService);
	}

	@Test
	@DisplayName("공연 상세 조회 테스트 - 성공 (200 OK)")
	@WithMockUser
	void getPerformanceDetail_Success() throws Exception {
		// given
		final UUID performanceId = UUID.randomUUID();

		Mockito.when(dynamicAuthService.canViewPerformanceDetail(Mockito.eq(performanceId), Mockito.any()))
			.thenReturn(true);

		PerformanceDetailResponse detail = PerformanceDetailResponse.builder()
			.id(performanceId)
			.imageUrl("https://example.com/image.jpg")
			.name("오페라의 유령")
			.type("MUSICAL")
			.genre("DRAMA")
			.averageStar(new BigDecimal("4.5"))
			.runningTime(150)
			.description("설명")
			.adultOnly(false)
			.ticketOpenTime(LocalDateTime.now().minusDays(10))
			.ticketCloseTime(LocalDateTime.now().plusDays(10))
			.stadium(StadiumResponse.builder().id(UUID.randomUUID()).name("부산문화회관").address("주소").build())
			.performanceSchedules(List.of())
			.seatSectionPrices(List.of())
			.build();

		Mockito.when(performanceService.getPerformanceDetail(performanceId)).thenReturn(detail);

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
	@DisplayName("공연 상세 조회 테스트 - 실패 (존재하지 않는 ID, 404 NOT FOUND)")
	@WithMockUser
	void getPerformanceDetail_NotFound() throws Exception {
		// given
		final UUID notExistId = UUID.randomUUID();

		Mockito.when(dynamicAuthService.canViewPerformanceDetail(Mockito.eq(notExistId), Mockito.any()))
			.thenReturn(true);

		Mockito.when(performanceService.getPerformanceDetail(notExistId))
			.thenThrow(new DomainException(ErrorCode.PERFORMANCE_NOT_FOUND));

		// when & then
		mockMvc.perform(get("/api/v1/performances/{id}", notExistId)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("공연 목록 조회 - 성공 (200 OK)")
	@WithMockUser
	void getPerformances_Success() throws Exception {
		// given
		UUID id = UUID.randomUUID();
		PerformanceSummaryResponse summary = new PerformanceSummaryResponse(
			id,
			"햄릿",
			"https://example.com/image2.jpg",
			PerformanceType.CLASSIC,
			PerformanceGenre.PLAY,
			"대전예술의전당",
			Date.valueOf(LocalDate.of(2025, 9, 1)),
			Date.valueOf(LocalDate.of(2025, 9, 30)),
			false
		);

		PaginationResponse pagination = PaginationResponse.builder()
			.page(0)
			.size(10)
			.totalItems(1)
			.totalPages(1)
			.hasNext(false)
			.hasPrevious(false)
			.build();

		PerformanceListResponse listResponse = PerformanceListResponse.builder()
			.performances(List.of(summary))
			.pagination(pagination)
			.build();

		Mockito.when(performanceService.getPerformanceList(Mockito.any())).thenReturn(listResponse);

		// when and then
		mockMvc.perform(get("/api/v1/performances")
				.param("page", "0").param("size", "10")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.pagination.totalItems").value(1))
			.andExpect(jsonPath("$.data.performances", hasSize(1)))
			.andExpect(jsonPath("$.data.performances[0].name").value("햄릿"))
			.andExpect(jsonPath("$.data.performances[0].startDate").value("2025-09-01"))
			.andExpect(jsonPath("$.data.performances[0].endDate").value("2025-09-30"));
	}
}
