package wisoft.nextframe.schedulereservationticketing.builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformanceStatistic;

public class PerformanceStatisticBuilder {

	private UUID performanceId; // 선택: 필요 시 직접 지정
	private Performance performance; // 보통 이 경로 사용
	private Integer hit = 0;
	private BigDecimal averageStar = BigDecimal.ZERO;
	private LocalDateTime updatedAt = LocalDateTime.now();

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

	public PerformanceStatisticBuilder withAverageStar(BigDecimal star) {
		this.averageStar = star;
		return this;
	}

	public PerformanceStatisticBuilder withUpdatedAt(LocalDateTime t) {
		this.updatedAt = t;
		return this;
	}

	public PerformanceStatistic build() {
		PerformanceStatistic.PerformanceStatisticBuilder b = PerformanceStatistic.builder()
			.hit(hit)
			.averageStar(averageStar)
			.updatedAt(updatedAt);

		if (performance != null) {
			b.performance(performance);
		}
		if (performanceId != null) {
			b.performanceId(performanceId);
		}

		return b.build();
	}
}
