package wisoft.nextframe.schedulereservationticketing.repository.stadium;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import wisoft.nextframe.schedulereservationticketing.entity.stadium.StadiumSection;

public interface StadiumSectionRepository extends JpaRepository<StadiumSection, UUID> {
}