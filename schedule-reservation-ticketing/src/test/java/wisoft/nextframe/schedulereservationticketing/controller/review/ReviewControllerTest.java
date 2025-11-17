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
import wisoft.nextframe.schedulereservationticketing.config.IntegrationTestContainersConfig;
import wisoft.nextframe.schedulereservationticketing.config.jwt.JwtTokenProvider;
import wisoft.nextframe.schedulereservationticketing.dto.review.ReviewCreateRequest;
import wisoft.nextframe.schedulereservationticketing.dto.review.ReviewUpdateRequest;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.reservation.Reservation;
import wisoft.nextframe.schedulereservationticketing.entity.review.Review;
import wisoft.nextframe.schedulereservationticketing.entity.review.ReviewLike;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.Stadium;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;
import wisoft.nextframe.schedulereservationticketing.repository.performance.PerformanceRepository;
import wisoft.nextframe.schedulereservationticketing.repository.reservation.ReservationRepository;
import wisoft.nextframe.schedulereservationticketing.repository.review.ReviewLikeRepository;
import wisoft.nextframe.schedulereservationticketing.repository.review.ReviewRepository;
import wisoft.nextframe.schedulereservationticketing.repository.schedule.ScheduleRepository;
import wisoft.nextframe.schedulereservationticketing.repository.stadium.StadiumRepository;
import wisoft.nextframe.schedulereservationticketing.repository.user.UserRepository;

@AutoConfigureMockMvc
class ReviewControllerTest extends IntegrationTestContainersConfig {

	@Autowired
	private ReviewRepository reviewRepository;
	@Autowired
	private ReviewLikeRepository reviewLikeRepository;

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

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Test
	@DisplayName("리뷰 생성 통합 테스트 - 성공 (201 CREATED)")
	void createReview_Success() throws Exception {
		// given: 사용자, 공연, 일정, 예매 데이터 준비
		User user = userRepository.save(User.builder().name("홍길동").build());
		Stadium stadium = stadiumRepository.save(StadiumBuilder.builder().withName("대전예술의전당").build());
		Performance performance = performanceRepository.save(PerformanceBuilder.builder().withName("햄릿").build());
		Schedule schedule = scheduleRepository.save(
			ScheduleBuilder.builder().withPerformance(performance).withStadium(stadium).build());
		reservationRepository.save(Reservation.create(user, schedule, 10000));

		UUID performanceId = performance.getId();

		ReviewCreateRequest request = new ReviewCreateRequest(BigDecimal.valueOf(4.5), "정말 멋진 공연!");
		String json = objectMapper.writeValueAsString(request);

		// AuthenticationPrincipal에 UUID를 넣기 위해 principal을 UUID로 구성
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user.getId(), null);

		// when & then
		mockMvc.perform(post("/api/v1/performances/{performanceId}/reviews", performanceId).with(authentication(auth))
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
		Stadium stadium = stadiumRepository.save(StadiumBuilder.builder().withName("부산문화회관").build());
		Performance performance = performanceRepository.save(PerformanceBuilder.builder().withName("오페라의 유령").build());
		Schedule schedule = scheduleRepository.save(
			ScheduleBuilder.builder().withPerformance(performance).withStadium(stadium).build());

		UUID performanceId = performance.getId();

		ReviewCreateRequest request = new ReviewCreateRequest(BigDecimal.valueOf(3.0), "재밌었어요");
		String json = objectMapper.writeValueAsString(request);

		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user.getId(), null);

		// when & then
		mockMvc.perform(post("/api/v1/performances/{performanceId}/reviews", performanceId).with(authentication(auth))
			.contentType(MediaType.APPLICATION_JSON)
			.content(json)).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
	}

	@Test
	@DisplayName("리뷰 목록 조회 통합 테스트 - 기본 페이지네이션 및 정렬, 좋아요 상태 포함 (200 OK)")
	void getReviews_DefaultPagination_AndLikeStatus() throws Exception {
		// given
		User viewer = userRepository.save(User.builder().name("관람자").build());
		Stadium stadium = stadiumRepository.save(StadiumBuilder.builder().withName("세종문화회관").build());
		Performance performance = performanceRepository.save(PerformanceBuilder.builder().withName("리어왕").build());
		Schedule schedule = scheduleRepository.save(
			ScheduleBuilder.builder().withPerformance(performance).withStadium(stadium).build());
		// 리뷰는 예매 여부와 무관하게 조회 가능하므로 예매는 필수 아님. 그래도 한 건 넣어둠
		reservationRepository.save(Reservation.create(viewer, schedule, 12000));

		// 작성자 2명 생성하고 6개의 리뷰 생성 (기본 size=5이므로 한 건은 다음 페이지)
		User author1 = userRepository.save(User.builder().name("작성자1").build());
		User author2 = userRepository.save(User.builder().name("작성자2").build());

		Review r1 = reviewRepository.save(
			Review.builder().performance(performance).user(author1).content("r1").likeCount(0).build());
		Review r2 = reviewRepository.save(
			Review.builder().performance(performance).user(author2).content("r2").likeCount(0).build());
		Review r3 = reviewRepository.save(
			Review.builder().performance(performance).user(author1).content("r3").likeCount(0).build());
		Review r4 = reviewRepository.save(
			Review.builder().performance(performance).user(author2).content("r4").likeCount(0).build());
		Review r5 = reviewRepository.save(
			Review.builder().performance(performance).user(author1).content("r5").likeCount(0).build());
		Review r6 = reviewRepository.save(
			Review.builder().performance(performance).user(author2).content("r6").likeCount(0).build()); // 가장 최신

		// viewer가 최신 리뷰 r6에 좋아요를 눌렀다고 가정
		reviewLikeRepository.save(new ReviewLike(r6, viewer));

		UUID performanceId = performance.getId();
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(viewer.getId(), null);

		// when & then
		mockMvc.perform(get("/api/v1/performances/{performanceId}/reviews", performanceId).with(authentication(auth)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			// 기본 size=5
			.andExpect(jsonPath("$.data.reviews", org.hamcrest.Matchers.hasSize(5)))
			// 정렬: createdAt DESC 이므로 가장 마지막에 저장한 r6가 첫 번째여야 함
			.andExpect(jsonPath("$.data.reviews[0].id").value(r6.getId().toString()))
			// 첫 번째 항목의 likeStatus 가 true (viewer가 좋아요)
			.andExpect(jsonPath("$.data.reviews[0].likeStatus").value(true))
			// 페이지네이션 기본값 확인
			.andExpect(jsonPath("$.data.pagination.page").value(0))
			.andExpect(jsonPath("$.data.pagination.size").value(5))
			.andExpect(jsonPath("$.data.pagination.totalItems").value(6))
			.andExpect(jsonPath("$.data.pagination.totalPages").value(2))
			.andExpect(jsonPath("$.data.pagination.hasNext").value(true))
			.andExpect(jsonPath("$.data.pagination.hasPrevious").value(false));
	}

	@Test
	@DisplayName("리뷰 목록 조회 통합 테스트 - 페이지/사이즈 파라미터 반영 (200 OK)")
	void getReviews_CustomPaginationParams() throws Exception {
		// given
		User user = userRepository.save(User.builder().name("사용자").build());
		Performance performance = performanceRepository.save(PerformanceBuilder.builder().withName("노트르담 드 파리").build());

		User author = userRepository.save(User.builder().name("작성자").build());
		// 7개 생성
		for (int i = 1; i <= 7; i++) {
			reviewRepository.save(
				Review.builder().performance(performance).user(author).content("c" + i).likeCount(0).build());
		}

		UUID performanceId = performance.getId();
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user.getId(), null);

		// when & then: page=1, size=3 이면 두 번째 페이지에 3건
		mockMvc.perform(get("/api/v1/performances/{performanceId}/reviews", performanceId).with(authentication(auth))
				.param("page", "1")
				.param("size", "3"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.data.reviews", org.hamcrest.Matchers.hasSize(3)))
			.andExpect(jsonPath("$.data.pagination.page").value(1))
			.andExpect(jsonPath("$.data.pagination.size").value(3))
			.andExpect(jsonPath("$.data.pagination.totalItems").value(7))
			.andExpect(jsonPath("$.data.pagination.totalPages").value(3))
			.andExpect(jsonPath("$.data.pagination.hasNext").value(true))
			.andExpect(jsonPath("$.data.pagination.hasPrevious").value(true));
	}

	@Test
	@DisplayName("리뷰 목록 조회 통합 테스트 - 리뷰가 없을 때 빈 목록과 0 카운트 반환 (200 OK)")
	void getReviews_Empty() throws Exception {
		// given
		User user = userRepository.save(User.builder().name("게스트").build());
		Performance performance = performanceRepository.save(PerformanceBuilder.builder().withName("무대").build());

		UUID performanceId = performance.getId();
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user.getId(), null);

		// when & then
		mockMvc.perform(get("/api/v1/performances/{performanceId}/reviews", performanceId).with(authentication(auth)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.data.reviews", org.hamcrest.Matchers.hasSize(0)))
			.andExpect(jsonPath("$.data.pagination.totalItems").value(0))
			.andExpect(jsonPath("$.data.pagination.totalPages").value(0))
			.andExpect(jsonPath("$.data.pagination.page").value(0));
	}

	@Test
	@DisplayName("리뷰 수정 통합 테스트 - 성공 (200 OK)")
	void updateReview_Success() throws Exception {
		// given: 리뷰 작성자와 리뷰 생성
		User author = userRepository.save(User.builder().name("작성자").build());
		Performance performance = performanceRepository.save(PerformanceBuilder.builder().withName("공연").build());
		Review review = reviewRepository.save(Review.builder()
			.performance(performance)
			.user(author)
			.star(BigDecimal.valueOf(3.0))
			.content("old content")
			.likeCount(0)
			.build());

		UUID reviewId = review.getId();
		// JWT 토큰 생성 (PATCH 요청은 인증 필요)
		String token = jwtTokenProvider.generateAccessToken(author.getId());

		ReviewUpdateRequest request = new ReviewUpdateRequest(BigDecimal.valueOf(4.5), "new content");
		String json = objectMapper.writeValueAsString(request);

		// when & then
		mockMvc.perform(patch("/api/v1/reviews/{reviewId}", reviewId).header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.data.id").value(reviewId.toString()));
	}

	@Test
	@DisplayName("리뷰 수정 통합 테스트 - 권한 없음 (403 FORBIDDEN)")
	void updateReview_Forbidden_WhenNotAuthor() throws Exception {
		// given: 작성자와 다른 사용자, 리뷰 생성
		User author = userRepository.save(User.builder().name("작성자").build());
		User other = userRepository.save(User.builder().name("다른사용자").build());
		Performance performance = performanceRepository.save(PerformanceBuilder.builder().withName("공연").build());
		Review review = reviewRepository.save(Review.builder()
			.performance(performance)
			.user(author)
			.star(BigDecimal.valueOf(2.5))
			.content("orig")
			.likeCount(0)
			.build());

		UUID reviewId = review.getId();
		// JWT 토큰 생성 (PATCH 요청은 인증 필요)
		String token = jwtTokenProvider.generateAccessToken(other.getId());

		ReviewUpdateRequest request = new ReviewUpdateRequest(BigDecimal.valueOf(4.0), "hacked");
		String json = objectMapper.writeValueAsString(request);

		// when & then
		mockMvc.perform(patch("/api/v1/reviews/{reviewId}", reviewId).header("Authorization", "Bearer " + token)
			.contentType(MediaType.APPLICATION_JSON)
			.content(json)).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
	}

	@Test
	@DisplayName("리뷰 수정 통합 테스트 - 대상 리뷰 없음 (404 NOT_FOUND)")
	void updateReview_NotFound() throws Exception {
		// given: 사용자만 생성하고 존재하지 않는 리뷰 ID 사용
		User user = userRepository.save(User.builder().name("사용자").build());
		// JWT 토큰 생성 (PATCH 요청은 인증 필요)
		String token = jwtTokenProvider.generateAccessToken(user.getId());

		ReviewUpdateRequest request = new ReviewUpdateRequest(BigDecimal.valueOf(1.0), "content");
		String json = objectMapper.writeValueAsString(request);

		UUID unknownId = UUID.randomUUID();

		// when & then
		mockMvc.perform(patch("/api/v1/reviews/{reviewId}", unknownId).header("Authorization", "Bearer " + token)
			.contentType(MediaType.APPLICATION_JSON)
			.content(json)).andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("NOT_FOUND"));
	}

	@Test
	@DisplayName("리뷰 삭제 통합 테스트 - 성공 (204 NO_CONTENT)")
	void deleteReview_Success() throws Exception {
		// given: 리뷰 작성자와 리뷰 생성
		User author = userRepository.save(User.builder().name("작성자").build());
		Performance performance = performanceRepository.save(PerformanceBuilder.builder().withName("공연").build());
		Review review = reviewRepository.save(Review.builder()
			.performance(performance)
			.user(author)
			.star(BigDecimal.valueOf(5.0))
			.content("to be deleted")
			.likeCount(0)
			.build());

		UUID reviewId = review.getId();
		String token = jwtTokenProvider.generateAccessToken(author.getId());

		// when & then
		mockMvc.perform(delete("/api/v1/reviews/{reviewId}", reviewId).header("Authorization", "Bearer " + token))
			.andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("리뷰 삭제 통합 테스트 - 권한 없음 (403 FORBIDDEN)")
	void deleteReview_Forbidden_WhenNotAuthor() throws Exception {
		// given: 작성자와 다른 사용자, 리뷰 생성
		User author = userRepository.save(User.builder().name("작성자").build());
		User other = userRepository.save(User.builder().name("다른사용자").build());
		Performance performance = performanceRepository.save(PerformanceBuilder.builder().withName("공연").build());
		Review review = reviewRepository.save(Review.builder()
			.performance(performance)
			.user(author)
			.star(BigDecimal.valueOf(2.0))
			.content("not yours")
			.likeCount(0)
			.build());

		UUID reviewId = review.getId();
		String token = jwtTokenProvider.generateAccessToken(other.getId());

		// when & then
		mockMvc.perform(delete("/api/v1/reviews/{reviewId}", reviewId).header("Authorization", "Bearer " + token))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("FORBIDDEN"));
	}

	@Test
	@DisplayName("리뷰 삭제 통합 테스트 - 대상 없음 (404 NOT_FOUND)")
	void deleteReview_NotFound() throws Exception {
		// given: 사용자만 생성하고 존재하지 않는 리뷰 ID 사용
		User user = userRepository.save(User.builder().name("사용자").build());
		String token = jwtTokenProvider.generateAccessToken(user.getId());
		UUID unknownId = UUID.randomUUID();

		// when & then
		mockMvc.perform(delete("/api/v1/reviews/{reviewId}", unknownId).header("Authorization", "Bearer " + token))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("NOT_FOUND"));
	}

	@Test
	@DisplayName("리뷰 좋아요 토글 통합 테스트 - 처음 누르면 좋아요 상태가 된다 (200 OK)")
	void toggleLike_FirstTime_LikeOn() throws Exception {
		// given: 리뷰와 사용자 준비
		User viewer = userRepository.save(User.builder().name("관람자").build());
		Performance performance = performanceRepository.save(PerformanceBuilder.builder().withName("공연A").build());
		User author = userRepository.save(User.builder().name("작성자").build());
		Review review = reviewRepository.save(
			Review.builder().performance(performance).user(author).content("content").likeCount(0).build());

		UUID reviewId = review.getId();
		String token = jwtTokenProvider.generateAccessToken(viewer.getId());

		// when & then
		mockMvc.perform(post("/api/v1/reviews/{reviewId}/likes", reviewId).header("Authorization", "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.data.likeStatus").value(true));

		// DB 검증: likeCount 증가 및 ReviewLike 저장
		Review updated = reviewRepository.findById(reviewId).orElseThrow();
		org.junit.jupiter.api.Assertions.assertEquals(1, updated.getLikeCount());
		org.junit.jupiter.api.Assertions.assertTrue(
			reviewLikeRepository.findByReviewIdAndUserId(reviewId, viewer.getId()).isPresent());
	}

	@Test
	@DisplayName("리뷰 좋아요 토글 통합 테스트 - 두 번 누르면 취소된다 (200 OK)")
	void toggleLike_Twice_TurnsOff() throws Exception {
		// given
		User viewer = userRepository.save(User.builder().name("관람자").build());
		Performance performance = performanceRepository.save(PerformanceBuilder.builder().withName("공연B").build());
		User author = userRepository.save(User.builder().name("작성자").build());
		Review review = reviewRepository.save(
			Review.builder().performance(performance).user(author).content("c").likeCount(0).build());

		UUID reviewId = review.getId();
		String token = jwtTokenProvider.generateAccessToken(viewer.getId());

		// 첫 번째 호출: 좋아요 ON
		mockMvc.perform(post("/api/v1/reviews/{reviewId}/likes", reviewId)
				.header("Authorization", "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.likeStatus").value(true));

		// 두 번째 호출: 좋아요 OFF
		mockMvc.perform(post("/api/v1/reviews/{reviewId}/likes", reviewId)
				.header("Authorization", "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.likeStatus").value(false));

		// DB 검증
		Review updated = reviewRepository.findById(reviewId).orElseThrow();
		org.junit.jupiter.api.Assertions.assertEquals(0, updated.getLikeCount());
		org.junit.jupiter.api.Assertions.assertTrue(
			reviewLikeRepository.findByReviewIdAndUserId(reviewId, viewer.getId()).isEmpty());
	}

	@Test
	@DisplayName("리뷰 좋아요 토글 통합 테스트 - 대상 리뷰 없음 (404 NOT_FOUND)")
	void toggleLike_NotFound() throws Exception {
		// given: 사용자만 생성
		User viewer = userRepository.save(User.builder().name("관람자").build());
		UUID unknown = UUID.randomUUID();
		String token = jwtTokenProvider.generateAccessToken(viewer.getId());

		// when & then
		mockMvc.perform(post("/api/v1/reviews/{reviewId}/likes", unknown)
				.header("Authorization", "Bearer " + token))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("NOT_FOUND"));
	}
}