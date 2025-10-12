package wisoft.nextframe.schedulereservationticketing.entity.performance;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@Entity
@Table(name = "performance_statistics")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PerformanceStatistic {

	@Id
	@Column(name = "performance_id", nullable = false)
	private UUID performanceId;

	@Column(name = "hit")
	private Integer hit;

	@Column(name = "average_star", precision = 2, scale = 1)
	private BigDecimal averageStar;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@OneToOne(fetch = FetchType.LAZY)
	@MapsId
	@JoinColumn(name = "performance_id")
	private Performance performance;
}
