package wisoft.nextframe.schedulereservationticketing.dto.ticketing;

import java.time.LocalDateTime;
import java.util.UUID;

public record TicketInfoResponse(
	UUID ticketId,
	LocalDateTime issuedAt,
	String qrCode,
	String performanceName,
	Integer rowNo,
	Integer columnNo
) {
	public String seatNumber() {
		return String.format("%d열 %d번", rowNo, columnNo);
	}
}
