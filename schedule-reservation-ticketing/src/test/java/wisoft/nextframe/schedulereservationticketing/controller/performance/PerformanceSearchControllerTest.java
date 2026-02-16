package wisoft.nextframe.schedulereservationticketing.controller.performance;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import wisoft.nextframe.schedulereservationticketing.config.jwt.JwtAuthenticationFilter;
import wisoft.nextframe.schedulereservationticketing.config.security.SecurityConfig;
import wisoft.nextframe.schedulereservationticketing.dto.performance.performancelist.response.PaginationResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performance.performancelist.response.PerformanceSummaryResponse;
import wisoft.nextframe.schedulereservationticketing.dto.performance.search.request.PerformanceSearchSort;
import wisoft.nextframe.schedulereservationticketing.dto.performance.search.response.PerformanceSearchResponse;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformanceGenre;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformanceType;
import wisoft.nextframe.schedulereservationticketing.service.auth.DynamicAuthService;
import wisoft.nextframe.schedulereservationticketing.service.performance.PerformanceService;

@WebMvcTest(value = PerformanceController.class,
	excludeFilters = @ComponentScan.Filter(
		type = FilterType.ASSIGNABLE_TYPE,
		classes = {SecurityConfig.class, JwtAuthenticationFilter.class}
	)
)
class PerformanceSearchControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private PerformanceService performanceService;

	@MockitoBean
	private DynamicAuthService dynamicAuthService;

	@Nested
	@DisplayName("공연 검색 API 테스트")
	class SearchPerformancesTest {

		@Test
		@DisplayName("키워드로 공연을 검색한다 - 성공 (200 OK)")
		@WithMockUser
		void searchPerformances_withKeyword_success() throws Exception {
			// given
			PerformanceSummaryResponse summary = new PerformanceSummaryResponse(
				UUID.randomUUID(),
				"햄릿",
				"https://example.com/hamlet.jpg",
				PerformanceType.CLASSIC,
				PerformanceGenre.PLAY,
				"대전예술의전당",
				LocalDate.of(2025, 9, 1),
				LocalDate.of(2025, 9, 30),
				false
			);

			PaginationResponse pagination = PaginationResponse.builder()
				.page(0)
				.size(32)
				.totalItems(1)
				.totalPages(1)
				.hasNext(false)
				.hasPrevious(false)
				.build();

			PerformanceSearchResponse searchResponse = PerformanceSearchResponse.builder()
				.keyword("햄릿")
				.performances(List.of(summary))
				.pagination(pagination)
				.build();

			when(performanceService.searchPerformances(eq("햄릿"), eq(PerformanceSearchSort.HIT_DESC), any()))
				.thenReturn(searchResponse);

			// when & then
			mockMvc.perform(get("/api/v1/performances/search")
					.param("keyword", "햄릿")
					.param("sort", "HIT_DESC")
					.param("page", "0")
					.param("size", "32")
					.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("SUCCESS"))
				.andExpect(jsonPath("$.data.keyword").value("햄릿"))
				.andExpect(jsonPath("$.data.performances", hasSize(1)))
				.andExpect(jsonPath("$.data.performances[0].name").value("햄릿"))
				.andExpect(jsonPath("$.data.performances[0].stadiumName").value("대전예술의전당"))
				.andExpect(jsonPath("$.data.performances[0].startDate").value("2025-09-01"))
				.andExpect(jsonPath("$.data.performances[0].endDate").value("2025-09-30"))
				.andExpect(jsonPath("$.data.pagination.totalItems").value(1))
				.andExpect(jsonPath("$.data.pagination.page").value(0))
				.andExpect(jsonPath("$.data.pagination.size").value(32));
		}

		@Test
		@DisplayName("키워드 없이 전체 검색한다 - 성공 (200 OK)")
		@WithMockUser
		void searchPerformances_withoutKeyword_success() throws Exception {
			// given
			PerformanceSearchResponse searchResponse = PerformanceSearchResponse.builder()
				.keyword(null)
				.performances(List.of())
				.pagination(PaginationResponse.builder()
					.page(0).size(32).totalItems(0).totalPages(0)
					.hasNext(false).hasPrevious(false).build())
				.build();

			when(performanceService.searchPerformances(isNull(), isNull(), any()))
				.thenReturn(searchResponse);

			// when & then
			mockMvc.perform(get("/api/v1/performances/search")
					.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("SUCCESS"))
				.andExpect(jsonPath("$.data.keyword").doesNotExist())
				.andExpect(jsonPath("$.data.performances", hasSize(0)))
				.andExpect(jsonPath("$.data.pagination.totalItems").value(0));
		}

		@Test
		@DisplayName("정렬 기준을 지정하여 검색한다 - 성공 (200 OK)")
		@WithMockUser
		void searchPerformances_withSort_success() throws Exception {
			// given
			PerformanceSearchResponse searchResponse = PerformanceSearchResponse.builder()
				.keyword(null)
				.performances(List.of())
				.pagination(PaginationResponse.builder()
					.page(0).size(32).totalItems(0).totalPages(0)
					.hasNext(false).hasPrevious(false).build())
				.build();

			when(performanceService.searchPerformances(isNull(), eq(PerformanceSearchSort.STAR_DESC), any()))
				.thenReturn(searchResponse);

			// when & then
			mockMvc.perform(get("/api/v1/performances/search")
					.param("sort", "STAR_DESC")
					.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("SUCCESS"));

			verify(performanceService).searchPerformances(isNull(), eq(PerformanceSearchSort.STAR_DESC), any());
		}

		@Test
		@DisplayName("페이지네이션 파라미터를 지정하여 검색한다 - 성공 (200 OK)")
		@WithMockUser
		void searchPerformances_withPagination_success() throws Exception {
			// given
			PaginationResponse pagination = PaginationResponse.builder()
				.page(2)
				.size(16)
				.totalItems(50)
				.totalPages(4)
				.hasNext(true)
				.hasPrevious(true)
				.build();

			PerformanceSearchResponse searchResponse = PerformanceSearchResponse.builder()
				.keyword("뮤지컬")
				.performances(List.of())
				.pagination(pagination)
				.build();

			when(performanceService.searchPerformances(eq("뮤지컬"), isNull(), any()))
				.thenReturn(searchResponse);

			// when & then
			mockMvc.perform(get("/api/v1/performances/search")
					.param("keyword", "뮤지컬")
					.param("page", "2")
					.param("size", "16")
					.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.pagination.page").value(2))
				.andExpect(jsonPath("$.data.pagination.size").value(16))
				.andExpect(jsonPath("$.data.pagination.totalItems").value(50))
				.andExpect(jsonPath("$.data.pagination.totalPages").value(4))
				.andExpect(jsonPath("$.data.pagination.hasNext").value(true))
				.andExpect(jsonPath("$.data.pagination.hasPrevious").value(true));
		}
	}
}
