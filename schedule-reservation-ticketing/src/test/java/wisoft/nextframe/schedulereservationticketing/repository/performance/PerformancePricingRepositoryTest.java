package wisoft.nextframe.schedulereservationticketing.repository.performance;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import wisoft.nextframe.schedulereservationticketing.builder.PerformanceBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.ScheduleBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.StadiumBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.StadiumSectionBuilder;
import wisoft.nextframe.schedulereservationticketing.config.DataJpaTestContainersConfig;
import wisoft.nextframe.schedulereservationticketing.config.DbConfig;
import wisoft.nextframe.schedulereservationticketing.dto.performance.performancedetail.response.SeatSectionPriceResponse;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformancePricing;
import wisoft.nextframe.schedulereservationticketing.entity.performance.PerformancePricingId;
import wisoft.nextframe.schedulereservationticketing.entity.schedule.Schedule;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.Stadium;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.StadiumSection;
import wisoft.nextframe.schedulereservationticketing.repository.schedule.ScheduleRepository;
import wisoft.nextframe.schedulereservationticketing.repository.stadium.StadiumRepository;
import wisoft.nextframe.schedulereservationticketing.repository.stadium.StadiumSectionRepository;

@DataJpaTest
@Import({DbConfig.class, DataJpaTestContainersConfig.class})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PerformancePricingRepositoryTest {

	@Autowired
	private PerformancePricingRepository performancePricingRepository;
	@Autowired
	private ScheduleRepository scheduleRepository;
	@Autowired
	private StadiumSectionRepository stadiumSectionRepository;
	@Autowired
	private PerformanceRepository performanceRepository;
	@Autowired
	private StadiumRepository stadiumRepository;

	private Stadium stadium;

	@BeforeEach
	void setUp() {
		stadium = stadiumRepository.save(StadiumBuilder.builder().build());
	}

	@Nested
	class findSeatSectionPricesTest {

		@Test
		@DisplayName("공연+공연장 기준으로 DISTINCT 및 가격 내림차순 정렬 검증")
		void findSeatSectionPrices_distinctAndOrder() {
			// given: 동일 Performance/ Stadium 에 대해 서로 다른 2개 스케줄을 만들고,
			// 각 스케줄에 동일 섹션(A/B) 가격을 설정하여 DISTINCT가 적용되는지 확인
			Performance performance = performanceRepository.save(PerformanceBuilder.builder().build());

			StadiumSection sectionA = stadiumSectionRepository.save(StadiumSectionBuilder.builder()
				.withStadium(stadium)
				.withSectionName("A")
				.build());
			StadiumSection sectionB = stadiumSectionRepository.save(StadiumSectionBuilder.builder()
				.withStadium(stadium)
				.withSectionName("B")
				.build());

			Schedule schedule1 = scheduleRepository.save(ScheduleBuilder.builder()
				.withPerformance(performance)
				.withStadium(stadium)
				.build());
			Schedule schedule2 = scheduleRepository.save(ScheduleBuilder.builder()
				.withPerformance(performance)
				.withStadium(stadium)
				.build());

			// 스케줄1에 A:150000, B:120000
			savePricing(schedule1, sectionA, 150000);
			savePricing(schedule1, sectionB, 120000);
			// 스케줄2에도 동일한 가격을 넣어 중복 유발 → DISTINCT로 2건만 나와야 함
			savePricing(schedule2, sectionA, 150000);
			savePricing(schedule2, sectionB, 120000);

			// when
			List<SeatSectionPriceResponse> results = performancePricingRepository.findSeatSectionPrices(
				performance.getId(), stadium.getId());

			// then: DISTINCT 적용으로 2건(A,B)만, 가격 내림차순(150000 → 120000)
			assertThat(results).hasSize(2);
			assertThat(results.get(0).price()).isEqualTo(150000);
			assertThat(results.get(0).section()).isEqualTo("A");
			assertThat(results.get(1).price()).isEqualTo(120000);
			assertThat(results.get(1).section()).isEqualTo("B");
		}
	}

	@Nested
	class findByScheduleIdAndSectionIdsTest {

		@Test
		@DisplayName("특정 스케줄과 섹션 집합에 해당하는 가격 조회")
		void findByScheduleIdAndSectionIds_returnsSubset() {
			// given
			Performance performance = performanceRepository.save(PerformanceBuilder.builder().build());

			StadiumSection sectionA = stadiumSectionRepository.save(
				StadiumSectionBuilder.builder().withStadium(stadium).withSectionName("A").build());
			StadiumSection sectionB = stadiumSectionRepository.save(
				StadiumSectionBuilder.builder().withStadium(stadium).withSectionName("B").build());

			Schedule schedule = scheduleRepository.save(ScheduleBuilder.builder()
				.withPerformance(performance)
				.withStadium(stadium)
				.build());

			savePricing(schedule, sectionA, 100000);
			savePricing(schedule, sectionB, 120000);

			List<UUID> targetSectionIds = new ArrayList<>();
			targetSectionIds.add(sectionA.getId());
			targetSectionIds.add(sectionB.getId());

			// when
			List<PerformancePricing> found = performancePricingRepository.findByScheduleIdAndSectionIds(
				schedule.getId(), targetSectionIds);

			// then: sectionA, sectionB 반환, 가격 검증
			assertThat(found).hasSize(2);
			assertThat(found).extracting(pp -> pp.getStadiumSection().getId())
				.containsExactlyInAnyOrder(sectionA.getId(), sectionB.getId());
			assertThat(found).filteredOn(pp -> pp.getStadiumSection().getId().equals(sectionB.getId()))
				.first().extracting(PerformancePricing::getPrice).isEqualTo(120000);
			assertThat(found).filteredOn(pp -> pp.getStadiumSection().getId().equals(sectionA.getId()))
				.first().extracting(PerformancePricing::getPrice).isEqualTo(100000);
		}
	}

	private void savePricing(Schedule schedule, StadiumSection section, int price) {
		final PerformancePricingId id = PerformancePricingId.builder()
			.scheduleId(schedule.getId())
			.stadiumSectionId(section.getId())
			.build();

		final PerformancePricing pricing = PerformancePricing.builder()
			.id(id)
			.schedule(schedule)
			.stadiumSection(section)
			.price(price)
			.build();

		performancePricingRepository.save(pricing);
	}
}