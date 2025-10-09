package wisoft.nextframe.schedulereservationticketing.repository.review;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import wisoft.nextframe.schedulereservationticketing.config.AbstractIntegrationTest;
import wisoft.nextframe.schedulereservationticketing.dto.review.ReviewItemResponse;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformanceGenre;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformanceType;
import wisoft.nextframe.schedulereservationticketing.entity.review.Review;
import wisoft.nextframe.schedulereservationticketing.entity.review.ReviewLike;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;
import wisoft.nextframe.schedulereservationticketing.repository.performance.PerformanceRepository;
import wisoft.nextframe.schedulereservationticketing.repository.user.UserRepository;

class ReviewRepositoryTest extends AbstractIntegrationTest {

	@Autowired
	private ReviewRepository reviewRepository;
	@Autowired
	private ReviewLikeRepository reviewLikeRepository;
	@Autowired
	private PerformanceRepository performanceRepository;
	@Autowired
	private UserRepository userRepository;

	@Test
	@DisplayName("성공: 특정 공연의 리뷰 목록을 조회하면 작성자 정보와 좋아요 여부가 포함된다")
	void findReviewsByPerformanceId_Success_WithLikeStatus() {
		// given
		Performance performance = performanceRepository.save(
			Performance.builder()
				.id(UUID.randomUUID())
				.name("테스트 공연")
				.type(PerformanceType.CLASSIC)
				.genre(PerformanceGenre.PLAY)
				.adultOnly(false)
				.imageUrl("http://example.com/perf.jpg")
				.description("설명")
				.build()
		);

		User viewer = userRepository.save(User.builder()
			.name("관람자")
			.email("viewer@example.com")
			.imageUrl("http://example.com/viewer.png")
			.build());

		User author1 = userRepository.save(User.builder()
			.name("작성자1")
			.email("author1@example.com")
			.imageUrl("http://example.com/author1.png")
			.build());

		User author2 = userRepository.save(User.builder()
			.name("작성자2")
			.email("author2@example.com")
			.imageUrl("http://example.com/author2.png")
			.build());

		Review review1 = reviewRepository.save(Review.builder()
			.performance(performance)
			.user(author1)
			.content("최고였어요")
			.star(new BigDecimal("4.5"))
			.likeCount(3)
			.build());

		Review review2 = reviewRepository.save(Review.builder()
			.performance(performance)
			.user(author2)
			.content("그럭저럭")
			.star(new BigDecimal("3.0"))
			.likeCount(1)
			.build());

		// viewer가 review1에 좋아요를 누름
		reviewLikeRepository.save(new ReviewLike(review1, viewer));

		Pageable pageable = PageRequest.of(0, 10);

		// when
		Page<ReviewItemResponse> page = reviewRepository.findReviewsByPerformanceId(
			performance.getId(), viewer.getId(), pageable);

		// then
		assertThat(page.getTotalElements()).isEqualTo(2);
		List<ReviewItemResponse> content = page.getContent();
		// 작성자 정보
		assertThat(content).anySatisfy(item -> {
			if (item.id().equals(review1.getId())) {
				assertThat(item.writerName()).isEqualTo("작성자1");
				assertThat(item.writerProfileImageUrl()).isEqualTo("http://example.com/author1.png");
				assertThat(item.content()).isEqualTo("최고였어요");
				assertThat(item.likeStatus()).isTrue();
				assertThat(item.likeCount()).isEqualTo(3);
				assertThat(item.createdAt()).isNotNull();
				assertThat(item.updatedAt()).isNotNull();
			}
		});
		assertThat(content).anySatisfy(item -> {
			if (item.id().equals(review2.getId())) {
				assertThat(item.writerName()).isEqualTo("작성자2");
				assertThat(item.writerProfileImageUrl()).isEqualTo("http://example.com/author2.png");
				assertThat(item.content()).isEqualTo("그럭저럭");
				assertThat(item.likeStatus()).isFalse();
				assertThat(item.likeCount()).isEqualTo(1);
				assertThat(item.createdAt()).isNotNull();
				assertThat(item.updatedAt()).isNotNull();
			}
		});
	}

	@Test
	@DisplayName("성공: 로그인하지 않은 경우 likeStatus는 항상 false이다")
	void findReviewsByPerformanceId_Success_AnonymousUser() {
		// given
		Performance performance = performanceRepository.save(
			Performance.builder()
				.id(UUID.randomUUID())
				.name("테스트 공연2")
				.type(PerformanceType.CLASSIC)
				.genre(PerformanceGenre.PLAY)
				.adultOnly(false)
				.imageUrl("http://example.com/perf2.jpg")
				.description("설명2")
				.build()
		);

		User author = userRepository.save(User.builder()
			.name("작성자")
			.email("author@example.com")
			.imageUrl("http://example.com/author.png")
			.build());

		reviewRepository.save(Review.builder()
			.performance(performance)
			.user(author)
			.content("내용")
			.star(new BigDecimal("5.0"))
			.likeCount(10)
			.build());

		Pageable pageable = PageRequest.of(0, 10);

		// when
		Page<ReviewItemResponse> page = reviewRepository.findReviewsByPerformanceId(
			performance.getId(), null, pageable);

		// then
		assertThat(page.getTotalElements()).isEqualTo(1);
		ReviewItemResponse item = page.getContent().getFirst();
		assertThat(item.likeStatus()).isFalse();
	}
}
