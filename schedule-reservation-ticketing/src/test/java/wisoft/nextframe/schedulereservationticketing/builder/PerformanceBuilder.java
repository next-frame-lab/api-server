package wisoft.nextframe.schedulereservationticketing.builder;

import java.time.Duration;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformanceGenre;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformanceType;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PerformanceBuilder {

	private UUID id = UUID.randomUUID();
	private String name = "테스트 공연";
	private PerformanceType type = PerformanceType.JAZZ;
	private PerformanceGenre genre = PerformanceGenre.PLAY;
	private boolean adultOnly = false;
	private Duration runningTime = Duration.ofMinutes(120);
	private String imageUrl = "http://example.com/image.jpg";
	private String description = "테스트 공연에 대한 설명";

	public static PerformanceBuilder builder() {
		return new PerformanceBuilder();
	}

	public PerformanceBuilder withId(UUID id) {
		this.id = id;
		return this;
	}

	public PerformanceBuilder withName(String name) {
		this.name = name;
		return this;
	}

	public PerformanceBuilder withAdultOnly(boolean adultOnly) {
		this.adultOnly = adultOnly;
		return this;
	}

	public Performance build() {
		return new Performance(id, name, type, genre, adultOnly, runningTime, imageUrl, description);
	}
}
