package wisoft.nextframe.schedulereservationticketing.schedule.repository.stadium;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.transaction.Transactional;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.Stadium;
import wisoft.nextframe.schedulereservationticketing.entity.stadium.StadiumSection;
import wisoft.nextframe.schedulereservationticketing.repository.stadium.SeatDefinitionRepository;
import wisoft.nextframe.schedulereservationticketing.repository.stadium.StadiumRepository;
import wisoft.nextframe.schedulereservationticketing.repository.stadium.StadiumSectionRepository;

@SpringBootTest
@Transactional
class SeatDefinitionRepositoryTest {

	@Autowired
	private SeatDefinitionRepository seatDefinitionRepository;
	@Autowired
	private StadiumRepository stadiumRepository;
	@Autowired
	private StadiumSectionRepository stadiumSectionRepository;
	private Stadium savedStadium;
	private StadiumSection savedSection;

	@BeforeEach
	void setUp() {
		Stadium stadium = Stadium.builder().id(UUID.randomUUID()).name("수원KT위즈파크").address("경기도 수원시 장안구").build();
		savedStadium = stadiumRepository.save(stadium);

		StadiumSection section = StadiumSection.builder().id(UUID.randomUUID()).stadium(savedStadium).section("A").build();
		savedSection = stadiumSectionRepository.save(section);
	}

	@Test
	@DisplayName("새로운 좌석 정의를 저장하고 ID로 조회하면 성공한다.")
	void saveAndFindById_test() {
		// given
		UUID seatId = UUID.randomUUID();
		SeatDefinition newSeat = SeatDefinition.builder()
			.id(seatId)
			.stadiumSection(savedSection)
			.rowNo(10)
			.columnNo(15)
			.build();

		// when
		seatDefinitionRepository.save(newSeat);
		Optional<SeatDefinition> foundSeatOptional = seatDefinitionRepository.findById(seatId);

		// then
		assertThat(foundSeatOptional).isPresent();

		SeatDefinition foundSeat = foundSeatOptional.get();
		assertThat(foundSeat.getId()).isEqualTo(seatId);
		assertThat(foundSeat.getRowNo()).isEqualTo(10);
		assertThat(foundSeat.getColumnNo()).isEqualTo(15);
		assertThat(foundSeat.getStadiumSection().getId()).isEqualTo(savedSection.getId());
	}
}