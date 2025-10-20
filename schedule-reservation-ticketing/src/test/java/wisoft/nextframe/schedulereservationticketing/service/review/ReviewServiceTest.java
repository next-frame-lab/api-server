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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import wisoft.nextframe.schedulereservationticketing.dto.review.ReviewCreateRequest;
import wisoft.nextframe.schedulereservationticketing.dto.review.ReviewCreateResponse;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.review.Review;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;
import wisoft.nextframe.schedulereservationticketing.exception.DomainException;
import wisoft.nextframe.schedulereservationticketing.repository.performance.PerformanceRepository;
import wisoft.nextframe.schedulereservationticketing.repository.reservation.ReservationRepository;
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

	private User testUser;
	private Performance testPerformance;
	private UUID userId;
	private UUID performanceId;

	@BeforeEach
	void setUp() {
		userId = UUID.randomUUID();
		performanceId = UUID.randomUUID();

		// 실제 엔티티가 아닌, 테스트용 객체 생성
		testUser = User.builder().id(userId).name("테스트유저").build();
		testPerformance = Performance.builder().id(performanceId).name("테스트공연").build();
	}

	@Test
	@DisplayName("리뷰 생성에 성공한다.")
	void createReview_Success() {
		// given
		ReviewCreateRequest request = new ReviewCreateRequest(BigDecimal.valueOf(4.5), "최고의 공연!");
		Review savedReview = Review.builder()
			.id(UUID.randomUUID())
			.user(testUser)
			.performance(testPerformance)
			.star(request.star())
			.content(request.content())
			.createdAt(LocalDateTime.now())
			.build();

		// Mock 객체들의 행동을 정의
		given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
		given(performanceRepository.findById(performanceId)).willReturn(Optional.of(testPerformance));
		given(reviewRepository.existsByPerformanceAndUser(testPerformance, testUser)).willReturn(false); // 중복 리뷰 없음
		given(reservationRepository.existsByUserAndPerformance(testUser, testPerformance)).willReturn(true); // 예매 내역 있음
		given(reviewRepository.save(any(Review.class))).willReturn(savedReview); // 저장 요청 시, 미리 정의된 review 객체 반환

		// when
		ReviewCreateResponse response = reviewService.createReview(performanceId, userId, request);

		// then
		assertThat(response).isNotNull();
		assertThat(response.id()).isEqualTo(savedReview.getId());
		verify(reviewRepository).save(any(Review.class)); // reviewRepository의 save 메서드가 호출되었는지 검증
	}

	@Test
	@DisplayName("실패 - 이미 리뷰를 작성한 경우")
	void createReview_Fail_WhenReviewAlreadyExists() {
		// given
		ReviewCreateRequest request = new ReviewCreateRequest(BigDecimal.valueOf(4.5), "두 번째 리뷰!");

		given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
		given(performanceRepository.findById(performanceId)).willReturn(Optional.of(testPerformance));
		given(reviewRepository.existsByPerformanceAndUser(testPerformance, testUser)).willReturn(true); // **중복 리뷰가 있다고 설정**

		// when and then
		assertThrows(DomainException.class, () -> {
			reviewService.createReview(performanceId, userId, request);
		});

		// 예외가 발생했으므로 save나 existsByUserAndPerformance는 호출되면 안 됨
		verify(reservationRepository, never()).existsByUserAndPerformance(any(), any());
		verify(reviewRepository, never()).save(any(Review.class));
	}

	@Test
	@DisplayName("실패 - 공연 예매 내역이 없는 경우")
	void createReview_Fail_WhenReservationNotFound() {
		// given
		ReviewCreateRequest request = new ReviewCreateRequest(BigDecimal.valueOf(3.0), "예매 안 하고 리뷰 쓰기");

		given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
		given(performanceRepository.findById(performanceId)).willReturn(Optional.of(testPerformance));
		given(reviewRepository.existsByPerformanceAndUser(testPerformance, testUser)).willReturn(false); // 중복 리뷰 없음
		given(reservationRepository.existsByUserAndPerformance(testUser, testPerformance)).willReturn(false); // **예매 내역이 없다고 설정**

		// when and then
		assertThrows(DomainException.class, () -> {
			reviewService.createReview(performanceId, userId, request);
		});

		// 예외가 발생했으므로 save는 호출되면 안 됨
		verify(reviewRepository, never()).save(any(Review.class));
	}
}