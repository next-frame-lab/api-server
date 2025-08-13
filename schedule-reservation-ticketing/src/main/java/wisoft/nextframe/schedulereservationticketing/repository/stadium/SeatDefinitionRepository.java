package wisoft.nextframe.schedulereservationticketing.repository.stadium;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import wisoft.nextframe.schedulereservationticketing.entity.stadium.SeatDefinition;

public interface SeatDefinitionRepository extends JpaRepository<SeatDefinition, UUID> {
}