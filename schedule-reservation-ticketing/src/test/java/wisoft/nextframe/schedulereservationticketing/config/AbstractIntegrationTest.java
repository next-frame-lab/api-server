package wisoft.nextframe.schedulereservationticketing.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

/**
 * 데이터베이스를 사용하는 모든 통합 테스트를 위한 추상 클래스입니다.
 * 이 클래스를 상속받는 테스트는 Testcontainers를 사용하여 격리된 PostgreSQL 데이터베이스 환경에서 실행됩니다.
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest implements PostgresSQLContainerInitializer {

	/**
	 * Testcontainers에 의해 동적으로 시작된 PostgreSQL 컨테이너 접속 정보를
	 * Spring의 ApplicationContext에 주입해주는 메서드입니다.
	 * 이를 통해 Spring은 테스트 실행 시점에 실제 컨테이너의 JDBC URL, 사용자 이름, 비밀번호를 알 수 있게 됩니다.
	 *
	 * @param registry Spring이 동적 프로퍼티를 등록할 수 있도록 제공하는 레지스트리
	 */
	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {

		// 인터페이스에 정의된 컨테이너 인스턴스(POSTGRES_CONTAINER)를 참조합니다.
		registry.add("spring.datasource.primary.jdbc-url", POSTGRES_CONTAINER::getJdbcUrl);
		registry.add("spring.datasource.primary.username", POSTGRES_CONTAINER::getUsername);
		registry.add("spring.datasource.primary.password", POSTGRES_CONTAINER::getPassword);
		registry.add("spring.datasource.primary.driver-class-name", () -> "org.postgresql.Driver");
	}
}
