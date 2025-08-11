package wisoft.nextframe.schedulereservationticketing.schedule.entity.performance;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class PerformancePricingId implements Serializable {

	@Column(name = "performance_id", nullable = false)
	private UUID performanceId;

	@Column(name = "stadium_section_id", nullable = false)
	private UUID stadiumSectionId;
}