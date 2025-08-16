package wisoft.nextframe.schedulereservationticketing.builder;

import java.time.Duration;
import java.util.UUID;

import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformanceGenre;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformanceType;

public class PerformanceBuilder {

	private UUID id = UUID.randomUUID();
	private String name = "테스트 공연";
	private PerformanceType type = PerformanceType.재즈;
	private PerformanceGenre genre = PerformanceGenre.연극;
	private Boolean adultOnly = false;
	private Duration runningTime = Duration.ofMinutes(120);
	private String imageUrl = "http://example.com/image.jpg";
	private String description = "테스트 공연에 대한 설명";

	public PerformanceBuilder withId(UUID id) {
		this.id = id;
		return this;
	}

	public PerformanceBuilder withName(String name) {
		this.name = name;
		return this;
	}

	public Performance build() {
		return new Performance(id, name, type, genre, adultOnly, runningTime, imageUrl, description);
	}
}
