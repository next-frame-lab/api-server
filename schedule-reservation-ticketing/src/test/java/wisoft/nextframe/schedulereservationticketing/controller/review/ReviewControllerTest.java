package wisoft.nextframe.schedulereservationticketing.controller.review;

import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import wisoft.nextframe.schedulereservationticketing.config.jwt.JwtAuthenticationFilter;
import wisoft.nextframe.schedulereservationticketing.config.security.SecurityConfig;
import wisoft.nextframe.schedulereservationticketing.dto.performance.performancelist.response.PaginationResponse;
import wisoft.nextframe.schedulereservationticketing.dto.review.ReviewCreateRequest;
import wisoft.nextframe.schedulereservationticketing.dto.review.ReviewCreateResponse;
import wisoft.nextframe.schedulereservationticketing.dto.review.ReviewLikeResponse;
import wisoft.nextframe.schedulereservationticketing.dto.review.ReviewListResponse;
import wisoft.nextframe.schedulereservationticketing.dto.review.ReviewUpdateRequest;
import wisoft.nextframe.schedulereservationticketing.dto.review.ReviewUpdateResponse;
import wisoft.nextframe.schedulereservationticketing.service.review.ReviewService;

@WebMvcTest(value = ReviewController.class,
	excludeFilters = @ComponentScan.Filter(
		type = FilterType.ASSIGNABLE_TYPE,
		classes = {SecurityConfig.class, JwtAuthenticationFilter.class}
	)
)
class ReviewControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private ReviewService reviewService;

	private final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private final UUID PERFORMANCE_ID = UUID.randomUUID();
	private final UUID REVIEW_ID = UUID.randomUUID();

	@BeforeEach
	void setUp() {
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
			USER_ID,
			null,
			Collections.emptyList()
		);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	@Nested
	@DisplayName("리뷰 생성 테스트")
	class createReviewTest {

		@Test
		@DisplayName("리뷰 생성 성공: 201 Created와 생성된 ID를 반환한다")
		void createReview_success() throws Exception {
			// given
			ReviewCreateRequest request = new ReviewCreateRequest(BigDecimal.valueOf(4.5), "리뷰 내용");
			ReviewCreateResponse response = new ReviewCreateResponse(REVIEW_ID, LocalDateTime.now());

			given(reviewService.createReview(eq(PERFORMANCE_ID), eq(USER_ID), any(ReviewCreateRequest.class)))
				.willReturn(response);

			// when and then
			mockMvc.perform(post("/api/v1/performances/{performanceId}/reviews", PERFORMANCE_ID)
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.code").value("SUCCESS"))
				.andExpect(jsonPath("$.data.id").value(REVIEW_ID.toString()));
		}

		@Test
		@DisplayName("리뷰 생성 실패: 별점이 0.5 미만이면 400 에러가 발생한다")
		void createReview_fail_min_star() throws Exception {
			// given
			ReviewCreateRequest request = new ReviewCreateRequest(BigDecimal.valueOf(0.1), "별점이 너무 낮아요.");

			// when and then
			mockMvc.perform(post("/api/v1/performances/{performanceId}/reviews", PERFORMANCE_ID)
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("BAD_REQUEST"));
		}
	}

	@Test
	@DisplayName("리뷰 목록 조회 성공: 200 OK와 리뷰 리스트를 반환한다")
	void getReviews_success() throws Exception {
		// given
		PaginationResponse pagination = PaginationResponse.builder()
			.page(0)
			.size(5)
			.totalItems(50)
			.totalPages(5)
			.hasNext(true)
			.hasPrevious(false)
			.build();
		ReviewListResponse response = new ReviewListResponse(List.of(), pagination);

		given(reviewService.getReviewsByPerformanceId(eq(PERFORMANCE_ID), eq(USER_ID), any(Pageable.class)))
			.willReturn(response);

		// when and then
		mockMvc.perform(get("/api/v1/performances/{performanceId}/reviews", PERFORMANCE_ID)
				.param("page", "0")
				.param("size", "5")
				.param("sort", "createdAt,desc"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.data.pagination.totalItems").value(50L));
	}

	@Test
	@DisplayName("리뷰 수정 성공: 200 OK와 수정된 정보를 반환한다")
	void updateReview_success() throws Exception {
		// given
		ReviewUpdateRequest request = new ReviewUpdateRequest(BigDecimal.valueOf(5.0), "내용을 수정합니다.");
		ReviewUpdateResponse response = new ReviewUpdateResponse(REVIEW_ID, LocalDateTime.now());

		given(reviewService.updateReview(eq(REVIEW_ID), eq(USER_ID), any(ReviewUpdateRequest.class)))
			.willReturn(response);

		// when and then
		mockMvc.perform(patch("/api/v1/reviews/{reviewId}", REVIEW_ID)
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.data.id").value(REVIEW_ID.toString()));
	}

	@Test
	@DisplayName("리뷰 삭제 성공: 204 No Content를 반환한다")
	void deleteReview_success() throws Exception {
		// when and then
		mockMvc.perform(delete("/api/v1/reviews/{reviewId}", REVIEW_ID)
				.with(csrf()))
			.andExpect(status().isNoContent());

		verify(reviewService).deleteReview(eq(REVIEW_ID), eq(USER_ID));
	}

	@Test
	@DisplayName("리뷰 좋아요 토글 성공: 200 OK를 반환한다")
	void toggleLike_success() throws Exception {
		// given
		ReviewLikeResponse response = new ReviewLikeResponse(true);

		given(reviewService.toggleReviewLike(eq(REVIEW_ID), eq(USER_ID)))
			.willReturn(response);

		// when and then
		mockMvc.perform(post("/api/v1/reviews/{reviewId}/likes", REVIEW_ID)
				.with(csrf()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.data.likeStatus").value(true));
	}
}