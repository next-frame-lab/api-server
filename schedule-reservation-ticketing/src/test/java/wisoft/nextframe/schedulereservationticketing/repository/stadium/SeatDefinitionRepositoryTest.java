package wisoft.nextframe.schedulereservationticketing.repository.stadium;

import static org.assertj.core.api.Assertions.*;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

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
import wisoft.nextframe.schedulereservationticketing.builder.SeatDefinitionBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.StadiumBuilder;
import wisoft.nextframe.schedulereservationticketing.builder.StadiumSectionBuilder;
import wisoft.nextframe.schedulereservationticketing.config.TestContainersConfig;
import wisoft.nextframe.schedulereservationticketing.entity.performance.Performance;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.Stadium;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.StadiumSection;
import wisoft.nextframe.schedulereservationticketing.repository.performance.PerformanceRepository;
import wisoft.nextframe.schedulereservationticketing.repository.schedule.ScheduleRepository;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestContainersConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SeatDefinitionRepositoryTest {

	@Autowired
	private SeatDefinitionRepository seatDefinitionRepository;
	@Autowired
	private StadiumSectionRepository stadiumSectionRepository;
	@Autowired
	private StadiumRepository stadiumRepository;
	@Autowired
	private PerformanceRepository performanceRepository;
	@Autowired
	private ScheduleRepository scheduleRepository;

	private Stadium targetStadium;
	private List<SeatDefinition> expectedSeats;

	private SeatDefinition seat_A_1_1;
	private SeatDefinition seat_A_1_2;
	private SeatDefinition seat_B_1_1;

	@BeforeEach
	void setUp() {
		targetStadium = stadiumRepository.save(StadiumBuilder.builder().withName("공연장1").build());
		Stadium otherStadium = stadiumRepository.save(StadiumBuilder.builder().withName("공연장2").build());

		Performance performance = performanceRepository.save(PerformanceBuilder.builder().build());
		scheduleRepository.save(ScheduleBuilder.builder().withStadium(targetStadium).withPerformance(performance).build());

		StadiumSection sectionA = stadiumSectionRepository.save(
			StadiumSectionBuilder.builder().withStadium(targetStadium).withSectionName("A").build());
		StadiumSection sectionB = stadiumSectionRepository.save(
			StadiumSectionBuilder.builder().withStadium(targetStadium).withSectionName("B").build());

		StadiumSection sectionC_Other = stadiumSectionRepository.save(
			StadiumSectionBuilder.builder().withStadium(otherStadium).withSectionName("C").build());

		seat_A_1_2 = seatDefinitionRepository.save(
			SeatDefinitionBuilder.builder().withStadiumSection(sectionA).withRowNo(1).withColumnNo(2).build());
		SeatDefinition seat_A_2_1 = seatDefinitionRepository.save(
			SeatDefinitionBuilder.builder().withStadiumSection(sectionA).withRowNo(2).withColumnNo(1).build());
		seat_B_1_1 = seatDefinitionRepository.save(
			SeatDefinitionBuilder.builder().withStadiumSection(sectionB).withRowNo(1).withColumnNo(1).build());
		SeatDefinition seat_B_1_2 = seatDefinitionRepository.save(
			SeatDefinitionBuilder.builder().withStadiumSection(sectionB).withRowNo(1).withColumnNo(2).build());
		seat_A_1_1 = seatDefinitionRepository.save(
			SeatDefinitionBuilder.builder().withStadiumSection(sectionA).withRowNo(1).withColumnNo(1).build());

		seatDefinitionRepository.save(
			SeatDefinitionBuilder.builder().withStadiumSection(sectionC_Other).withRowNo(1).withColumnNo(1).build());

		expectedSeats = Stream.of(
				seat_A_1_1, seat_A_1_2, seat_A_2_1,
				seat_B_1_1, seat_B_1_2
			)
			.sorted(Comparator
				.comparing((SeatDefinition sd) -> sd.getStadiumSection().getSection())
				.thenComparing(SeatDefinition::getRowNo)
				.thenComparing(SeatDefinition::getColumnNo))
			.toList();
	}

	@Nested
	class findAllByStadiumIdWithSortingTest {

		@Test
		@DisplayName("특정 공연장 ID로 모든 좌석 정보를 구역, 행, 열 순으로 오름차순 정렬하여 조회한다")
		void findAllByStadiumIdWithSorting_SuccessAndOrderCheck() {
			// given
			UUID stadiumId = targetStadium.getId();

			// when
			List<SeatDefinition> resultList = seatDefinitionRepository.findAllByStadiumIdWithSorting(stadiumId);

			// then
			// 조회된 결과의 크기가 공연장의 속한 좌석의 수와 일치하는지 검증
			assertThat(resultList).hasSize(expectedSeats.size());

			// 다른 공연장 좌석이 포함되지 않았는지 검증
			resultList.forEach(
				seat -> assertThat(seat.getStadiumSection().getStadium().getId()).isEqualTo(stadiumId));

			// 정렬 순서 검증
			assertThat(resultList)
				.usingElementComparator(Comparator
					.comparing((SeatDefinition sd) -> sd.getStadiumSection().getSection())
					.thenComparing(SeatDefinition::getRowNo)
					.thenComparing(SeatDefinition::getColumnNo)
				)
				.containsExactlyElementsOf(expectedSeats);
		}

		@Test
		@DisplayName("존재하지 않은 공연장 ID로 조회 시, 빈 목록을 반환한다")
		void findAllByStadiumIdWithSorting_NotFound() {
			// given
			UUID nonExistentStadiumId = UUID.randomUUID();

			// when
			List<SeatDefinition> resultList = seatDefinitionRepository.findAllByStadiumIdWithSorting(
				nonExistentStadiumId);

			// then
			assertThat(resultList).isEmpty();
		}
	}

	@Nested
	class findWithStadiumSectionByIdInTest {

		@Test
		@DisplayName("여러 좌석 ID에 해당하는 좌석 정보를 조회한다")
		void findWithStadiumSectionByIdIn_SuccessAndFetchJoinCheck() {
			// given
			List<UUID> targetSeatIds = List.of(seat_A_1_1.getId(), seat_B_1_1.getId());

			// when
			List<SeatDefinition> resultList = seatDefinitionRepository.findWithStadiumSectionByIdIn(targetSeatIds);

			// then
			// 결과 크기 검증
			assertThat(resultList).hasSize(2);

			// 반환된 ID 목록 검증
			List<UUID> resultIds = resultList.stream().map(SeatDefinition::getId).toList();
			assertThat(resultIds).containsExactlyElementsOf(targetSeatIds);
		}

		@Test
		@DisplayName("요청된 좌석 ID 목록에 존재하지 않는 ID가 포함되어도 존재하는 좌석만 조회된다")
		void findWithStadiumSectionByIdIn_PartialMatch() {
			// given
			UUID nonExistentId = UUID.randomUUID();
			List<UUID> targetSeatIds = List.of(seat_A_1_2.getId(), nonExistentId);

			// when
			List<SeatDefinition> resultList = seatDefinitionRepository.findWithStadiumSectionByIdIn(targetSeatIds);

			// then
			// 결과 크기는 존재하는 좌석 수인 1개여야 한다.
			assertThat(resultList).hasSize(1);

			// 반환된 좌석의 ID는 존재하는 ID와 일치해야 한다.
			assertThat(resultList.getFirst().getId()).isEqualTo(seat_A_1_2.getId());
		}
	}
}