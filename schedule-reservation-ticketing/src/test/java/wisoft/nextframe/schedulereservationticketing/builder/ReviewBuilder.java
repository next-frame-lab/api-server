package wisoft.nextframe.schedulereservationticketing.builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.review.Review;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewBuilder {

	private UUID id = null;
	private Performance performance;
	private User user;
	private String content = "테스트 댓글 내용";
	private BigDecimal star = BigDecimal.valueOf(4.5);
	private Integer likeCount = 0;
	private LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
	private LocalDateTime updatedAt = LocalDateTime.now();

	public static ReviewBuilder builder() {
		return new ReviewBuilder();
	}

	public ReviewBuilder withId(UUID id) {
		this.id = id;
		return this;
	}

	public ReviewBuilder withPerformance(Performance performance) {
		this.performance = performance;
		return this;
	}

	public ReviewBuilder withUser(User user) {
		this.user = user;
		return this;
	}

	public ReviewBuilder withContent(String content) {
		this.content = content;
		return this;
	}

	public ReviewBuilder withStar(BigDecimal star) {
		this.star = star;
		return this;
	}

	public ReviewBuilder withLikeCount(Integer likeCount) {
		this.likeCount = likeCount;
		return this;
	}

	public ReviewBuilder withCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
		return this;
	}

	public ReviewBuilder withUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
		return this;
	}

	public Review build() {
		return new Review(id, performance, user, content, star, likeCount, createdAt, updatedAt);
	}
}
