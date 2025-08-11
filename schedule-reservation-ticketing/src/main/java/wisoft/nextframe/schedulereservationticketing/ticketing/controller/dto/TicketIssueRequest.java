package wisoft.nextframe.schedulereservationticketing.ticketing.controller.dto;

import java.util.UUID;

public record TicketIssueRequest(UUID reservationId, UUID paymentId) {
}
