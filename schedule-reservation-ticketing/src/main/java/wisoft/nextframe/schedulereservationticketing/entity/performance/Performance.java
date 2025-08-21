package wisoft.nextframe.schedulereservationticketing.entity.performance;

import java.time.Duration;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.entity.performance.typeconverter.DurationMinutesConverter;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;
import wisoft.nextframe.schedulereservationticketing.exception.reservation.ReservationException;

@Getter
@Builder
@AllArgsConstructor
@Entity
@Table(name = "performances")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Performance {

	@Id
	@Column(name = "id", nullable = false)
	private UUID id;

	@Column(name = "name", nullable = false)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(name = "type")
	private PerformanceType type;

	@Enumerated(EnumType.STRING)
	@Column(name = "genre")
	private PerformanceGenre genre;

	@Column(name = "adult_only", nullable = false)
	private Boolean adultOnly;

	@Convert(converter = DurationMinutesConverter.class)
	@Column(name = "running_time")
	private Duration runningTime;

	@Column(name = "image_url")
	private String imageUrl;

	@Column(name = "description", columnDefinition = "text")
	private String description;

	public void verifyAgeLimit(User user) {
		if (this.adultOnly && !user.isAdult()) {
			throw new ReservationException("성인만 예매 가능한 공연입니다.");
		}
	}
}
