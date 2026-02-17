package wisoft.nextframe.payment.infra.config;

import org.testcontainers.containers.PostgreSQLContainer;

/**
 * PostgreSQL 컨테이너 초기화 인터페이스
 * 전체 테스트 병렬 실행을 위해 컨테이너를 단 한 번만 초기화 합니다.
 */
public class PostgresSQLContainerInitializer {

	static {
		System.setProperty("api.version", "1.44");
	}

	/**
	 * PostgreSQL 컨테이너를 생성하고 실행시키는 메서드입니다.
	 *
	 * @return PostgreSQLContainer 인스턴스
	 */

	static final PostgreSQLContainer<?> POSTGRES_CONTAINER =
		new PostgreSQLContainer<>("postgres:15")
			.withDatabaseName("testdb")
			.withUsername("testuser")
			.withPassword("testpass");

	static PostgreSQLContainer<?> getContainer() {
		if (!POSTGRES_CONTAINER.isRunning())
			POSTGRES_CONTAINER.start();
		return POSTGRES_CONTAINER;
	}
}