package wisoft.nextframe.ticketing.infra.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import wisoft.nextframe.ticketing.infra.TicketEntity;

@Repository
public interface JpaTicketRepository extends JpaRepository<TicketEntity, UUID> {
}