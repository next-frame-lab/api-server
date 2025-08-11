package wisoft.nextframe.schedulereservationticketing.schedule.repository.stadium;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.transaction.Transactional;
import wisoft.nextframe.schedulereservationticketing.schedule.entity.stadium.Stadium;

@SpringBootTest
@Transactional
class StadiumRepositoryTest {

	@Autowired
	private StadiumRepository stadiumRepository;

	@Test
	@DisplayName("새로운 경기장을 저장하고 ID로 조회하면 성공한다.")
	void saveAndFindById_test() {
		// given
		UUID stadiumId = UUID.randomUUID();
		Stadium newStadium = Stadium.builder()
			.id(stadiumId)
			.name("서울월드컵경기장")
			.address("서울시 마포구 성산동")
			.build();

		// when
		stadiumRepository.save(newStadium);
		Optional<Stadium> foundStadiumOptional = stadiumRepository.findById(stadiumId);

		// then
		assertThat(foundStadiumOptional).isPresent();

		Stadium foundStadium = foundStadiumOptional.get();
		assertThat(foundStadium.getId()).isEqualTo(stadiumId);
		assertThat(foundStadium.getName()).isEqualTo("서울월드컵경기장");
		assertThat(foundStadium.getAddress()).isEqualTo("서울시 마포구 성산동");
	}
}