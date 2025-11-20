package wisoft.nextframe.schedulereservationticketing.service.review;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import wisoft.nextframe.schedulereservationticketing.builder.PerformanceBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.ReviewBuilder;
import wisoft.nextframe.schedulereservationticketing.common.exception.DomainException;
import wisoft.nextframe.schedulereservationticketing.common.exception.ErrorCode;
import wisoft.nextframe.schedulereservationticketing.dto.review.ReviewCreateRequest;
import wisoft.nextframe.schedulereservationticketing.dto.review.ReviewCreateResponse;
import wisoft.nextframe.schedulereservationticketing.dto.review.ReviewLikeResponse;
import wisoft.nextframe.schedulereservationticketing.dto.review.ReviewUpdateRequest;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.review.Review;
import wisoft.nextframe.schedulereservationticketing.entity.review.ReviewLike;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;
import wisoft.nextframe.schedulereservationticketing.repository.performance.PerformanceRepository;
import wisoft.nextframe.schedulereservationticketing.repository.reservation.ReservationRepository;
import wisoft.nextframe.schedulereservationticketing.repository.review.ReviewLikeRepository;
import wisoft.nextframe.schedulereservationticketing.repository.review.ReviewRepository;
import wisoft.nextframe.schedulereservationticketing.repository.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

	@InjectMocks
	private ReviewService reviewService;

	@Mock
	private ReviewRepository reviewRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private PerformanceRepository performanceRepository;
	@Mock
	private ReservationRepository reservationRepository;
	@Mock
	private ReviewLikeRepository reviewLikeRepository;

	private User user;
	private Performance performance;
	private UUID userId;
	private UUID performanceId;

	@BeforeEach
	void setUp() {
		userId = UUID.randomUUID();
		performanceId = UUID.randomUUID();

		user = User.builder().id(userId).name("테스트유저").build();
		performance = PerformanceBuilder.builder().build();
	}

	@Nested
	class createReviewTest {

		@Test
		@DisplayName("리뷰 생성에 성공한다.")
		void createReview_Success() {
			// given
			ReviewCreateRequest request = new ReviewCreateRequest(BigDecimal.valueOf(4.5), "최고의 공연!");
			Review savedReview = Review.builder()
				.id(UUID.randomUUID())
				.user(user)
				.performance(performance)
				.star(request.star())
				.content(request.content())
				.createdAt(LocalDateTime.now())
				.build();

			// Mock 객체들의 행동을 정의
			given(userRepository.findById(userId)).willReturn(Optional.of(user));
			given(performanceRepository.findById(performanceId)).willReturn(Optional.of(performance));
			given(reviewRepository.existsByPerformanceAndUser(performance, user)).willReturn(false); // 중복 리뷰 없음
			given(reservationRepository.existsByUserAndPerformance(user, performance)).willReturn(true); // 예매 내역 있음
			given(reviewRepository.save(any(Review.class))).willReturn(savedReview); // 저장 요청 시, 미리 정의된 review 객체 반환

			// when
			ReviewCreateResponse response = reviewService.createReview(performanceId, userId, request);

			// then
			assertThat(response).isNotNull();
			assertThat(response.id()).isEqualTo(savedReview.getId());
			verify(reviewRepository).save(any(Review.class)); // reviewRepository의 save 메서드가 호출되었는지 검증
		}

		@Test
		@DisplayName("리뷰 생성 실패 - 이미 리뷰를 작성한 경우")
		void createReview_Fail_WhenReviewAlreadyExists() {
			// given
			ReviewCreateRequest request = new ReviewCreateRequest(BigDecimal.valueOf(4.5), "두 번째 리뷰!");

			given(userRepository.findById(userId)).willReturn(Optional.of(user));
			given(performanceRepository.findById(performanceId)).willReturn(Optional.of(performance));
			given(reviewRepository.existsByPerformanceAndUser(performance, user)).willReturn(true); // 중복 리뷰가 있다고 설정

			// when and then
			assertThrows(DomainException.class, () -> {
				reviewService.createReview(performanceId, userId, request);
			});

			// 예외가 발생했으므로 save나 existsByUserAndPerformance는 호출되면 안 됨
			verify(reservationRepository, never()).existsByUserAndPerformance(any(), any());
			verify(reviewRepository, never()).save(any(Review.class));
		}

		@Test
		@DisplayName("리뷰 생성 실패 - 공연 예매 내역이 없는 경우")
		void createReview_Fail_WhenReservationNotFound() {
			// given
			ReviewCreateRequest request = new ReviewCreateRequest(BigDecimal.valueOf(3.0), "예매 안 하고 리뷰 쓰기");

			given(userRepository.findById(userId)).willReturn(Optional.of(user));
			given(performanceRepository.findById(performanceId)).willReturn(Optional.of(performance));
			given(reviewRepository.existsByPerformanceAndUser(performance, user)).willReturn(false); // 중복 리뷰 없음
			given(reservationRepository.existsByUserAndPerformance(user, performance)).willReturn(false); // 예매 내역이 없다고 설정

			// when and then
			assertThrows(DomainException.class, () -> {
				reviewService.createReview(performanceId, userId, request);
			});

			// 예외가 발생했으므로 save는 호출되면 안 됨
			verify(reviewRepository, never()).save(any(Review.class));
		}
	}

	@Nested
	@DisplayName("리뷰 수정 테스트")
	class updateReviewTest {

		@Test
		@DisplayName("리뷰 수정 실패 - 작성자가 아닌 경우 권한 없음 예외 발생")
		void updateReview_Fail_WhenAccessDenied() {
			// given
			UUID reviewId = UUID.randomUUID();
			UUID otherUserId = UUID.randomUUID(); // 다른 사용자 ID
			ReviewUpdateRequest updateRequest = new ReviewUpdateRequest(BigDecimal.valueOf(1.0), "악플");

			User otherUser = User.builder().id(otherUserId).name("다른유저").build();

			// 다른 사용자가 작성한 리뷰
			Review otherReview = Review.builder()
				.id(reviewId)
				.user(otherUser)
				.build();

			given(reviewRepository.findById(reviewId)).willReturn(Optional.of(otherReview));

			// when and then
			assertThatThrownBy(() -> reviewService.updateReview(reviewId, userId, updateRequest))
				.isInstanceOf(DomainException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
		}

		@Test
		@DisplayName("리뷰 수정 실패 - 존재하지 않는 리뷰")
		void updateReview_Fail_WhenReviewNotFound() {
			// given
			UUID reviewId = UUID.randomUUID();
			ReviewUpdateRequest updateRequest = new ReviewUpdateRequest(BigDecimal.valueOf(5.0), "내용");

			given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

			// when and then
			assertThatThrownBy(() -> reviewService.updateReview(reviewId, userId, updateRequest))
				.isInstanceOf(DomainException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.REVIEW_NOT_FOUND);
		}
	}

	@Nested
	@DisplayName("리뷰 좋아요 테스트")
	class toggleReviewLikeTest {

		@Test
		@DisplayName("좋아요 등록 성공 - 기존에 좋아요가 없는 경우")
		void toggleLike_AddLike_Success() {
			// given
			UUID reviewId = UUID.randomUUID();
			Review review = ReviewBuilder.builder().withPerformance(performance).withUser(user).build();

			given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
			given(userRepository.findById(userId)).willReturn(Optional.of(user));

			// 기존 좋아요 없음
			given(reviewLikeRepository.findByReviewIdAndUserId(reviewId, userId)).willReturn(Optional.empty());

			// when
			ReviewLikeResponse response = reviewService.toggleReviewLike(reviewId, userId);

			// then
			assertThat(response.likeStatus()).isTrue();
			verify(reviewLikeRepository).save(any(ReviewLike.class)); // 저장이 호출되어야 함
		}

		@Test
		@DisplayName("좋아요 취소 성공 - 이미 좋아요가 있는 경우")
		void toggleLike_RemoveLike_Success() {
			// given
			UUID reviewId = UUID.randomUUID();
			Review review = ReviewBuilder.builder().withPerformance(performance).withUser(user).build();
			ReviewLike existingLike = new ReviewLike(review, user);

			given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
			given(userRepository.findById(userId)).willReturn(Optional.of(user));

			// 기존 좋아요 있음
			given(reviewLikeRepository.findByReviewIdAndUserId(reviewId, userId)).willReturn(Optional.of(existingLike));

			// when
			ReviewLikeResponse response = reviewService.toggleReviewLike(reviewId, userId);

			// then
			assertThat(response.likeStatus()).isFalse();
			verify(reviewLikeRepository).delete(existingLike);
		}
	}
}