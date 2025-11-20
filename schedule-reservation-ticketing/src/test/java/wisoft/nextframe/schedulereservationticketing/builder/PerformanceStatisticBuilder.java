package wisoft.nextframe.schedulereservationticketing.builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformanceStatistic;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PerformanceStatisticBuilder {

	private UUID performanceId = null;
	private Integer hit = 0;
	private BigDecimal averageStar = BigDecimal.ZERO;
	private LocalDateTime updatedAt = LocalDateTime.now();
	private Performance performance;

	public static PerformanceStatisticBuilder builder() {
		return new PerformanceStatisticBuilder();
	}

	public PerformanceStatisticBuilder withPerformance(Performance performance) {
		this.performance = performance;
		return this;
	}

	public PerformanceStatisticBuilder withPerformanceId(UUID id) {
		this.performanceId = id;
		return this;
	}

	public PerformanceStatisticBuilder withHit(int hit) {
		this.hit = hit;
		return this;
	}

	public PerformanceStatisticBuilder withAverageStar(BigDecimal averageStar) {
		this.averageStar = averageStar;
		return this;
	}

	public PerformanceStatisticBuilder withUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
		return this;
	}

	public PerformanceStatistic build() {
		return new PerformanceStatistic(performanceId, hit, averageStar, updatedAt, performance);
	}
}
