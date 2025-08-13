package wisoft.nextframe.schedulereservationticketing.schedule.repository.performance;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.transaction.Transactional;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformanceGenre;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformanceType;
import wisoft.nextframe.schedulereservationticketing.repository.performance.PerformanceRepository;

@SpringBootTest
@Transactional
class PerformanceRepositoryTest {

	@Autowired
	private PerformanceRepository performanceRepository;

	@Test
	@DisplayName("새로운 공연을 저장하고 ID로 조회하면 성공한다.")
	void saveAndFindById_test() {
		// given
		UUID performanceId = UUID.randomUUID();
		Duration runningTime = Duration.ofMinutes(150);
		Performance newPerformance = Performance.builder()
			.id(performanceId)
			.name("오페라의 유령")
			.type(PerformanceType.ROMANCE)
			.genre(PerformanceGenre.MUSICAL)
			.adultOnly(false)
			.runningTime(runningTime)
			.imageUrl("http://example.com/phantom_of_the_opera.jpg")
			.description("파리 오페라 하우스를 배경으로 한 미스터리 로맨스")
			.build();

		// when
		performanceRepository.save(newPerformance);
		Optional<Performance> foundPerformanceOptional = performanceRepository.findById(performanceId);

		// then
		assertThat(foundPerformanceOptional).isPresent();

		Performance foundPerformance = foundPerformanceOptional.get();
		assertThat(foundPerformance.getId()).isEqualTo(performanceId);
		assertThat(foundPerformance.getName()).isEqualTo("오페라의 유령");
		assertThat(foundPerformance.getType()).isEqualTo(PerformanceType.ROMANCE);
		assertThat(foundPerformance.getGenre()).isEqualTo(PerformanceGenre.MUSICAL);
		assertThat(foundPerformance.getAdultOnly()).isFalse();
		assertThat(foundPerformance.getRunningTime()).isEqualTo(runningTime);
		assertThat(foundPerformance.getImageUrl()).isEqualTo("http://example.com/phantom_of_the_opera.jpg");
		assertThat(foundPerformance.getDescription()).isEqualTo("파리 오페라 하우스를 배경으로 한 미스터리 로맨스");
	}
}