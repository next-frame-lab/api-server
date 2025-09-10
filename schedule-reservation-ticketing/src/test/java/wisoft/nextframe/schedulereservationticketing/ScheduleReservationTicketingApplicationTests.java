package wisoft.nextframe.schedulereservationticketing;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
	"spring.datasource.url=jdbc:postgresql://postgres:5432/testdb",
	"spring.datasource.username=testuser",
	"spring.datasource.password=testpassword",
	"spring.datasource.driver-class-name=org.postgresql.Driver",
	"spring.jpa.hibernate.ddl-auto=create-drop" // 테스트 환경에서는 validate 대신 create-drop이 안전합니다.
})
@SpringBootTest
class ScheduleReservationTicketingApplicationTests {

	@Test
	void contextLoads() {
	}

}
