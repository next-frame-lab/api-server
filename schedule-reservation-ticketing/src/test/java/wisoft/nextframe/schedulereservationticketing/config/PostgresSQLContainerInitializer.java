package wisoft.nextframe.schedulereservationticketing.config;

import org.testcontainers.containers.PostgreSQLContainer;

/**
 * PostgreSQL 컨테이너 초기화 인터페이스
 * 전체 테스트 병렬 실행을 위해 컨테이너를 단 한 번만 초기화 합니다.
 */
public interface PostgresSQLContainerInitializer {

	PostgreSQLContainer<?> POSTGRES_CONTAINER = startContainer();

	/**
	 * PostgreSQL 컨테이너를 생성하고 실행시키는 메서드입니다.
	 * @return PostgreSQLContainer 인스턴스
	 */
	private static PostgreSQLContainer<?> startContainer() {
		// 컨테이너의 인스턴스를 생성합니다.
		PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
			.withDatabaseName("testdb")
			.withUsername("testuser")
			.withPassword("testpass");

		// 컨테이너를 시작합니다.
		postgres.start();

		// 시작된 컨테이너 인스턴스를 반환합니다.
		return postgres;
	}
}
