package wisoft.nextframe.schedulereservationticketing.schedule.infra;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import wisoft.nextframe.schedule.domain.performance.Performance;
import wisoft.nextframe.schedule.infra.performance.PerformanceEntity;
import wisoft.nextframe.schedule.infra.performance.mapper.PerformanceMapper;

class PerformanceMapperTest {

	private final PerformanceMapper mapper = new PerformanceMapper();

	@Test
	@DisplayName("엔티티->도메인 매핑 성공")
	void should_map_entity_to_domain_correctly() {
		// given
		var entity = PerformanceEntityFixture.sampleEntity();

		// when
		Performance domain = mapper.toDomain(entity);

		// then
		assertThat(domain.getId().getValue()).isEqualTo(entity.getId());
		assertThat(domain.getProfile().getName()).isEqualTo(entity.getName());
		assertThat(domain.getProfile().getDescription()).isEqualTo(entity.getDescription());
		assertThat(domain.getProfile().getRunningTime()).isEqualTo(Duration.ofMinutes(entity.getRunningTime()));

		assertThat(domain.getProfile().getImageUrl()).isEqualTo(entity.getImageUrl());
		assertThat(domain.getProfile().getGenre().name()).isEqualTo(entity.getGenre());
		assertThat(domain.getProfile().getType().name()).isEqualTo(entity.getType());
		assertThat(domain.getProfile().isAdultOnly()).isEqualTo(entity.isAdultOnly());
	}

	@Test
	@DisplayName("도메인->엔티티 매핑 성공")
	void should_map_domain_to_entity_correctly() {
		// given
		Performance domain = PerformanceEntityFixture.sampleDomain();

		// when
		PerformanceEntity entity = mapper.toEntity(domain);

		// then
		assertThat(entity.getId()).isEqualTo(domain.getId().getValue());
		assertThat(entity.getName()).isEqualTo(domain.getProfile().getName());
		assertThat(entity.getDescription()).isEqualTo(domain.getProfile().getDescription());
		assertThat(entity.getRunningTime()).isEqualTo((int)domain.getProfile().getRunningTime().toMinutes());
		assertThat(entity.getImageUrl()).isEqualTo(domain.getProfile().getImageUrl());
		assertThat(entity.getGenre()).isEqualTo(domain.getProfile().getGenre().name());
		assertThat(entity.getType()).isEqualTo(domain.getProfile().getType().name());
		assertThat(entity.isAdultOnly()).isEqualTo(domain.getProfile().isAdultOnly());
	}
}
