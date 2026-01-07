package wisoft.nextframe.schedulereservationticketing.config.db;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "wisoft.nextframe.schedulereservationticketing.repository")
public class DbConfig {

	@Bean
	@Primary
	@ConfigurationProperties(prefix = "spring.datasource.primary")
	public DataSource primaryDataSource() {
		return DataSourceBuilder.create().build();
	}

	// todo: 다중 DB 및 JPA 도입 시 추가 설정이 필요함(ex EntityManagerFactory, TransactionManager)
	// @Bean
	// @ConfigurationProperties(prefix = "spring.datasource.replica")
	// public DataSource replicaDataSource() {
	// 	return DataSourceBuilder.create().build();
	// }
}
