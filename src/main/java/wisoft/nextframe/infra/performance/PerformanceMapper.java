package wisoft.nextframe.infra.performance;

import java.time.Duration;

import wisoft.nextframe.common.mapper.EntityMapper;
import wisoft.nextframe.domain.performance.Performance;
import wisoft.nextframe.domain.performance.PerformanceGenre;
import wisoft.nextframe.domain.performance.PerformanceId;
import wisoft.nextframe.domain.performance.PerformanceProfile;
import wisoft.nextframe.domain.performance.PerformanceType;

public class PerformanceMapper implements EntityMapper<Performance, PerformanceEntity> {

	// TODO: Schedule, sectionPrice, Stadium은 추후 Join 후 주입
	@Override
	public Performance toDomain(PerformanceEntity entity) {
		// TODO: Schedule is not yet implemented; passing null for now.
		return Performance.reconstruct(
			PerformanceId.of(entity.getId()),
			PerformanceProfile.of(
				entity.getName(),
				entity.getDescription(),
				Duration.ofMinutes(entity.getRunningTime()),
				entity.getImageUrl(),
				PerformanceGenre.valueOf(entity.getGenre()),
				PerformanceType.valueOf(entity.getType()),
				entity.isAdultOnly()
			),
			null,
			null,
			null,
			null
		);
	}

	@Override
	public PerformanceEntity toEntity(Performance domain) {
		return PerformanceEntity.builder()
			.id(domain.getId().getValue())
			.name(domain.getProfile().getName())
			.genre(domain.getProfile().getGenre().name())
			.type(domain.getProfile().getType().name())
			.adultOnly(domain.getProfile().isAdultOnly())
			.runningTime((int)domain.getProfile().getRunningTime().toMinutes())
			.imageUrl(domain.getProfile().getImageUrl())
			.description(domain.getProfile().getDescription())
			.build();

	}

}
