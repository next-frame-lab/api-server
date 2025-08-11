package wisoft.nextframe.schedulereservationticketing.schedule.repository.stadium;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import wisoft.nextframe.schedulereservationticketing.schedule.entity.stadium.StadiumSection;

public interface StadiumSectionRepository extends JpaRepository<StadiumSection, UUID> {
}