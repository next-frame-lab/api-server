package wisoft.nextframe.schedulereservationticketing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableCaching
@EnableScheduling
@SpringBootApplication(exclude = RedisRepositoriesAutoConfiguration.class)
@ConfigurationPropertiesScan
public class ScheduleReservationTicketingApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScheduleReservationTicketingApplication.class, args);
	}

}
