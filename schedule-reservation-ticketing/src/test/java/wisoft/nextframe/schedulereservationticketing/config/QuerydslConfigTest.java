package wisoft.nextframe.schedulereservationticketing.config;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import wisoft.nextframe.schedulereservationticketing.entity.user.QUser;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class QuerydslConfigTest {

	@Autowired
	EntityManager em;

	@Test
	public void contextLoads() {
		JPAQueryFactory queryFactory = new JPAQueryFactory(em);

		QUser user = QUser.user;

		List<User> result = queryFactory.selectFrom(user)
			.fetch();

		assertThat(result).isEmpty();
	}
}
