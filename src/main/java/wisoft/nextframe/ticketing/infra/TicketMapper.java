package wisoft.nextframe.ticketing.infra;

import java.util.UUID;

import wisoft.nextframe.reservation.domain.ReservationId;
import wisoft.nextframe.ticketing.domain.Ticket;
import wisoft.nextframe.ticketing.domain.TicketId;

public class TicketMapper {

	public Ticket toDomain(TicketEntity entity) {
		return Ticket.reconstruct(
			TicketId.of(entity.getId()),
			ReservationId.of(entity.getReservationId()),
			entity.getQrCode(),
			entity.getIssuedAt()
		);
	}

	public TicketEntity toEntity(Ticket domain, UUID seatId, UUID scheduleId) {
		return TicketEntity.builder()
			.id(domain.getTicketId().getValue())
			.reservationId(domain.getReservationId().getValue())
			.seatId(seatId)
			.scheduleId(scheduleId)
			.qrCode(domain.getQrCode())
			.issuedAt(domain.getIssuedAt())
			.isUsed(false) // 임시 값입니다. 구현 필요
			.build();
	}
}
