package wisoft.nextframe.schedulereservationticketing.entity.review;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;

@Entity
@Table(name = "review_likes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewLike {

	@EmbeddedId
	private ReviewLikeId id;

	@MapsId("reviewId")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "review_id")
	private Review review;

	@MapsId("userId")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@CreationTimestamp
	@Column(name = "liked_at", updatable = false)
	private LocalDateTime likedAt;

	public ReviewLike(Review review, User user) {
		this.review = review;
		this.user = user;
		// 복합 키(EmbeddedId)를 직접 생성하고 할당합니다.
		this.id = new ReviewLikeId(review.getId(), user.getId());
	}
}
