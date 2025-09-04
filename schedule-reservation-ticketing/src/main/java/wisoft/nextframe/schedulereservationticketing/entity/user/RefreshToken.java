package wisoft.nextframe.schedulereservationticketing.entity.user;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "refresh_tokens")
public class RefreshToken {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", columnDefinition = "uuid", updatable = false)
	private UUID id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "token_value", nullable = false, unique = true)
	private String tokenValue;

	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	@Builder
	public RefreshToken(User user, String tokenValue, LocalDateTime expiresAt) {
		this.user = user;
		this.tokenValue = tokenValue;
		this.expiresAt = expiresAt;
	}

	public void updateTokenValue(String newTokenValue, LocalDateTime newExpiresAt) {
		this.tokenValue = newTokenValue;
		this.expiresAt = newExpiresAt;
	}
}
