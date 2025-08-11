package wisoft.nextframe.ticketing.infra.repository;

import java.util.UUID;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import wisoft.nextframe.ticketing.application.port.TicketRepository;
import wisoft.nextframe.ticketing.domain.Ticket;
import wisoft.nextframe.ticketing.infra.TicketMapper;

@Repository
@RequiredArgsConstructor
public class TicketRepositoryImpl implements TicketRepository {

	private final JpaTicketRepository jpaTicketRepository;
	private final TicketMapper ticketMapper;

	@Override
	public Ticket save(Ticket ticket, UUID reservationId, UUID seatId, UUID scheduleId) {
		var entity = ticketMapper.toEntity(ticket, reservationId, seatId, scheduleId);
		var save = jpaTicketRepository.save(entity);
		return ticketMapper.toDomain(save);
	}
}
