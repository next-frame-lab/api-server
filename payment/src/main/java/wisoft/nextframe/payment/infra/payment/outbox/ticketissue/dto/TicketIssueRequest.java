package wisoft.nextframe.payment.infra.payment.outbox.ticketissue.dto;

import java.util.UUID;

public record TicketIssueRequest(UUID reservationId) {
}
