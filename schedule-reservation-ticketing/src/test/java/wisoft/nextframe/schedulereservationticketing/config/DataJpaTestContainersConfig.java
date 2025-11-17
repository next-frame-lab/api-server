package wisoft.nextframe.schedulereservationticketing.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * @DataJpaTest 슬라이스에서 Testcontainers(PostgreSQL) 연결 정보를 주입하기 위한 공용 설정.
 *
 * 사용법:
 *  - @DataJpaTest 클래스에 {@code @Import(DataJpaTestContainersConfig.class)} 추가
 *  - DbConfig가 사용하는 spring.datasource.primary.* 프리픽스를 그대로 사용합니다.
 */
@TestConfiguration
public class DataJpaTestContainersConfig implements PostgresSQLContainerInitializer {

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.primary.jdbc-url", POSTGRES_CONTAINER::getJdbcUrl);
        r.add("spring.datasource.primary.username", POSTGRES_CONTAINER::getUsername);
        r.add("spring.datasource.primary.password", POSTGRES_CONTAINER::getPassword);
        r.add("spring.datasource.primary.driver-class-name", () -> "org.postgresql.Driver");
    }
}
