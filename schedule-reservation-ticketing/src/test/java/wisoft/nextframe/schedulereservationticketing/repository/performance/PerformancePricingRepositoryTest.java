package wisoft.nextframe.schedulereservationticketing.repository.performance;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import wisoft.nextframe.schedulereservationticketing.builder.ScheduleBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.StadiumSectionBuilder;
import wisoft.nextframe.schedulereservationticketing.config.AbstractIntegrationTest;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformancePricing;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformancePricingId;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.StadiumSection;
import wisoft.nextframe.schedulereservationticketing.repository.schedule.ScheduleRepository;
import wisoft.nextframe.schedulereservationticketing.repository.stadium.StadiumSectionRepository;

class PerformancePricingRepositoryTest extends AbstractIntegrationTest {

	@Autowired
	private PerformancePricingRepository performancePricingRepository;
	@Autowired
	private ScheduleRepository scheduleRepository;
	@Autowired
	private StadiumSectionRepository stadiumSectionRepository;

	private Schedule savedSchedule;
	private StadiumSection savedSection;

	@BeforeEach
	void setUp() {
		savedSection = stadiumSectionRepository.save(new StadiumSectionBuilder().build());

		savedSchedule = scheduleRepository.save(new ScheduleBuilder().build());
	}

	@Test
	@DisplayName("성공: 새로운 공연 가격을 저장하고 복합키로 조회하면 성공한다")
	void saveAndFindById_Success() {
		// given
		PerformancePricingId pricingId = PerformancePricingId.builder()
			.scheduleId(savedSchedule.getId())
			.stadiumSectionId(savedSection.getId())
			.build();

		PerformancePricing newPricing = PerformancePricing.builder()
			.id(pricingId)
			.schedule(savedSchedule)
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
		assertThat(foundPricing.getSchedule().getId()).isEqualTo(savedSchedule.getId());
		assertThat(foundPricing.getStadiumSection().getId()).isEqualTo(savedSection.getId());
	}
}