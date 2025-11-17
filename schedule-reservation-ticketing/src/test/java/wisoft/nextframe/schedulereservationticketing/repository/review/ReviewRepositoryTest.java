package wisoft.nextframe.schedulereservationticketing.repository.review;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import wisoft.nextframe.schedulereservationticketing.builder.PerformanceBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.ReviewBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.UserBuilder;
import wisoft.nextframe.schedulereservationticketing.config.DataJpaTestContainersConfig;
import wisoft.nextframe.schedulereservationticketing.config.DbConfig;
import wisoft.nextframe.schedulereservationticketing.dto.review.ReviewItemResponse;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.review.Review;
import wisoft.nextframe.schedulereservationticketing.entity.review.ReviewLike;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;
import wisoft.nextframe.schedulereservationticketing.repository.performance.PerformanceRepository;
import wisoft.nextframe.schedulereservationticketing.repository.user.UserRepository;

@DataJpaTest
@Import({DbConfig.class, DataJpaTestContainersConfig.class})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReviewRepositoryTest {

	@Autowired
	private ReviewRepository reviewRepository;
	@Autowired
	private ReviewLikeRepository reviewLikeRepository;
	@Autowired
	private PerformanceRepository performanceRepository;
	@Autowired
	private UserRepository userRepository;

	private Performance performance;

	@BeforeEach
	void setUp() {
		performance = performanceRepository.save(PerformanceBuilder.builder().build());
	}

	@Nested
	class findReviewsByPerformanceIdTest {

		@Test
		@DisplayName("특정 공연의 리뷰 목록을 조회하면 작성자 정보와 좋아요 여부가 포함된다")
		void findReviewsByPerformanceId_Success_WithLikeStatus() {
			// given
			User viewer = userRepository.save(UserBuilder.builder().build());
			User author1 = userRepository.save(UserBuilder.builder().withName("작성자1").build());
			User author2 = userRepository.save(UserBuilder.builder().withName("작성자2").build());

			Review review1 = reviewRepository.save(Review.builder()
				.performance(performance)
				.user(author1)
				.content("작성자1가 작성한 리뷰 내용")
				.star(new BigDecimal("4.5"))
				.likeCount(3)
				.build());
			Review review2 = reviewRepository.save(Review.builder()
				.performance(performance)
				.user(author2)
				.content("작성자2가 작성한 리뷰 내용")
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

			assertThat(content).anySatisfy(item -> {
				if (item.id().equals(review1.getId())) {
					assertThat(item.writerName()).isEqualTo("작성자1");
					assertThat(item.content()).isEqualTo("작성자1가 작성한 리뷰 내용");
					assertThat(item.likeStatus()).isTrue();
					assertThat(item.likeCount()).isEqualTo(3);
					assertThat(item.createdAt()).isNotNull();
					assertThat(item.updatedAt()).isNotNull();
				}
			});
			assertThat(content).anySatisfy(item -> {
				if (item.id().equals(review2.getId())) {
					assertThat(item.writerName()).isEqualTo("작성자2");
					assertThat(item.content()).isEqualTo("작성자2가 작성한 리뷰 내용");
					assertThat(item.likeStatus()).isFalse();
					assertThat(item.likeCount()).isEqualTo(1);
					assertThat(item.createdAt()).isNotNull();
					assertThat(item.updatedAt()).isNotNull();
				}
			});
		}

		@Test
		@DisplayName("로그인하지 않은 경우 likeStatus는 항상 false이다")
		void findReviewsByPerformanceId_Success_AnonymousUser() {
			// given
			User author = userRepository.save(UserBuilder.builder().withName("작성자").build());
			reviewRepository.save(ReviewBuilder.builder().withPerformance(performance).withUser(author).build());
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
}
