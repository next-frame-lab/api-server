package wisoft.nextframe.schedulereservationticketing.schedule.infra;

import java.time.Duration;
import java.util.UUID;

import wisoft.nextframe.schedule.domain.performance.Performance;
import wisoft.nextframe.schedule.domain.performance.PerformanceGenre;
import wisoft.nextframe.schedule.domain.performance.PerformanceId;
import wisoft.nextframe.schedule.domain.performance.PerformanceProfile;
import wisoft.nextframe.schedule.domain.performance.PerformanceType;
import wisoft.nextframe.schedule.infra.performance.PerformanceEntity;

public class PerformanceEntityFixture {

	public static final UUID DEFAULT_ID = UUID.fromString("f1000000-aaaa-bbbb-cccc-000000000001");

	public static PerformanceEntity sampleEntity() {
		return PerformanceEntity.builder()
			.id(DEFAULT_ID)
			.name("아라딘")
			.genre("MUSICAL")
			.type("HORROR")
			.adultOnly(false)
			.runningTime(150)
			.imageUrl("https://image.example.com/aladdin.jpg")
			.description("환상의 이야기")
			.build();
	}

	public static Performance sampleDomain() {
		return Performance.reconstruct(
			PerformanceId.of(PerformanceEntityFixture.DEFAULT_ID),
			PerformanceProfile.of(
				"알라딘",
				"환상적인 이야기",
				Duration.ofMinutes(150),
				"https://image.example.com/aladdin.jpg",
				PerformanceGenre.MUSICAL,
				PerformanceType.HORROR,
				false
			),
			null,
			null,
			null,
			null
		);
	}
}
