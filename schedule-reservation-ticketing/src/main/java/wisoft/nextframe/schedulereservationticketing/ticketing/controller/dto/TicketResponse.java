package wisoft.nextframe.schedulereservationticketing.ticketing.controller.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import wisoft.nextframe.schedulereservationticketing.ticketing.entity.Ticket;

public record TicketResponse(UUID ticketId, String qrCode, LocalDateTime issuedAt) {
	public static TicketResponse from(Ticket ticket) {
		return new TicketResponse(ticket.getId(), ticket.getQrCode(), ticket.getIssuedAt());
	}
}
