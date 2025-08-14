package wisoft.nextframe.schedulereservationticketing.schedule.repository.performance;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ch.qos.logback.classic.turbo.TurboFilter;
import jakarta.transaction.Transactional;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformancePricing;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformancePricingId;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.Stadium;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.StadiumSection;
import wisoft.nextframe.schedulereservationticketing.repository.performance.PerformancePricingRepository;
import wisoft.nextframe.schedulereservationticketing.repository.performance.PerformanceRepository;
import wisoft.nextframe.schedulereservationticketing.repository.stadium.StadiumRepository;
import wisoft.nextframe.schedulereservationticketing.repository.stadium.StadiumSectionRepository;

@SpringBootTest
@Transactional
class PerformancePricingRepositoryTest {

	@Autowired
	private PerformancePricingRepository performancePricingRepository;
	@Autowired
	private PerformanceRepository performanceRepository;
	@Autowired
	private StadiumSectionRepository stadiumSectionRepository;
	@Autowired
	private StadiumRepository stadiumRepository;

	private Performance savedPerformance;
	private StadiumSection savedSection;

	@BeforeEach
	void setUp() {
		Stadium stadium = stadiumRepository.save(
			Stadium.builder().id(UUID.randomUUID()).name("블루스퀘어").address("대전광역시").build());

		savedSection = stadiumSectionRepository.save(
			StadiumSection.builder().id(UUID.randomUUID()).stadium(stadium).section("A").build());

		savedPerformance = performanceRepository.save(
			Performance.builder()
				.id(UUID.randomUUID())
				.name("레미제라블")
				.adultOnly(true)
				.runningTime(Duration.ofMinutes(180))
				.build()
		);
	}

	@Test
	@DisplayName("새로운 공연 가격을 저장하고 복합키로 조회하면 성공한다.")
	void saveAndFindById_test() {
		// given
		PerformancePricingId pricingId = PerformancePricingId.builder()
			.performanceId(savedPerformance.getId())
			.stadiumSectionId(savedSection.getId())
			.build();

		PerformancePricing newPricing = PerformancePricing.builder()
			.id(pricingId)
			.performance(savedPerformance)
			.stadiumSection(savedSection)
			.price(180000)
			.build();

		// when
		performancePricingRepository.save(newPricing);
		Optional<PerformancePricing> foundPricingOptional = performancePricingRepository.findById(pricingId);

		// then
		assertThat(foundPricingOptional).isPresent();

		PerformancePricing foundPricing = foundPricingOptional.get();
		assertThat(foundPricing.getId()).isEqualTo(pricingId);
		assertThat(foundPricing.getPrice()).isEqualTo(180000);
		assertThat(foundPricing.getPerformance().getId()).isEqualTo(savedPerformance.getId());
		assertThat(foundPricing.getStadiumSection().getId()).isEqualTo(savedSection.getId());
	}
}