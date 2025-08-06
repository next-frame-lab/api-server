package wisoft.nextframe.dbconn;

import static org.assertj.core.api.Assertions.*;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@DisplayName("DB 연동 테스트")
public class DatabaseConnectionTest {

	@Autowired
	@Qualifier("primaryDataSource")
	DataSource primaryDataSource;

	// @Autowired
	// @Qualifier("replicaDataSource")
	// DataSource replicaDataSource;

	@Test
	void primaryConnection() throws SQLException {
		try (Connection conn = primaryDataSource.getConnection()) {
			assertThat(conn.isValid(1)).isTrue();
		}
	}

	// @Test
	// void replicaConnection() throws SQLException {
	// 	try (Connection conn = replicaDataSource.getConnection()) {
	// 		assertThat(conn.isValid(1)).isTrue();
	// 	}
	// }
}
