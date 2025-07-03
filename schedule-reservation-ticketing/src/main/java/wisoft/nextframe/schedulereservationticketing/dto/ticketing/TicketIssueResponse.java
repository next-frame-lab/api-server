package wisoft.nextframe.schedulereservationticketing.dto.ticketing;

import java.time.LocalDateTime;
import java.util.UUID;

import wisoft.nextframe.schedulereservationticketing.entity.ticketing.Ticket;

public record TicketIssueResponse(UUID ticketId, String qrCode, LocalDateTime issuedAt) {
	public static TicketIssueResponse from(Ticket ticket) {
		return new TicketIssueResponse(ticket.getId(), ticket.getQrCode(), ticket.getIssuedAt());
	}
}
