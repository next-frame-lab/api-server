package wisoft.nextframe.schedulereservationticketing.schedule.repository.stadium;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import wisoft.nextframe.schedulereservationticketing.schedule.entity.stadium.Stadium;

public interface StadiumRepository extends JpaRepository<Stadium, UUID> {
}