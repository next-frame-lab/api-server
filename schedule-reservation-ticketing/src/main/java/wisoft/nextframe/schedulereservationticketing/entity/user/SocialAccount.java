package wisoft.nextframe.schedulereservationticketing.entity.user;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 사용자의 소셜 로그인 계정 정보를 관리하는 엔티티입니다.
 * 한 명의 사용자(User)는 여러 개의 소셜 계정(SocialAccount)을 가질 수 있습니다. (1:N 관계)
 * 이를 통해 여러 소셜 로그인 제공자(카카오, 구글 등)를 통한 계정 연동을 지원합니다.
 */
@Getter
@ToString(exclude = "user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(
	name = "social_accounts",
	uniqueConstraints = {
		@UniqueConstraint(columnNames = {"provider", "provider_user_id"})
	}
)
public class SocialAccount {

	/**
	 * 소셜 계정의 고유 식별자 (PK)
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(columnDefinition = "uuid", updatable = false, nullable = false)
	private UUID id;

	/**
	 * 소셜 로그인 제공자 이름 (e.g., "kakao", "google")
	 */
	@Column(nullable = false)
	private String provider;

	/**
	 * 해당 소셜 로그인 제공자 내에서 사용자를 식별하는 고유 ID
	 */
	@Column(name = "provider_user_id", nullable = false)
	private String providerUserId;

	/**
	 * 이 소셜 계정과 연동된 내부 사용자 계정 (FK)
	 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
}
