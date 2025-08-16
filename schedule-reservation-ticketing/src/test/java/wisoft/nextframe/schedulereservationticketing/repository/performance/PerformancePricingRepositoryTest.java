package wisoft.nextframe.schedulereservationticketing.repository.performance;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.transaction.Transactional;
import wisoft.nextframe.schedulereservationticketing.builder.PerformanceBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.StadiumBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.StadiumSectionBuilder;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformancePricing;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformancePricingId;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.Stadium;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.StadiumSection;
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
		savedSection = stadiumSectionRepository.save(new StadiumSectionBuilder().build());

		savedPerformance = performanceRepository.save(new PerformanceBuilder().build());
	}

	@Test
	@DisplayName("성공: 새로운 공연 가격을 저장하고 복합키로 조회하면 성공한다")
	void saveAndFindById_Success() {
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