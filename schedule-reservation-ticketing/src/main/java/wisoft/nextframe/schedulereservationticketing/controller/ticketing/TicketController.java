package wisoft.nextframe.schedulereservationticketing.controller.ticketing;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.dto.ticketing.TicketIssueRequest;
import wisoft.nextframe.schedulereservationticketing.dto.ticketing.TicketIssueResponse;
import wisoft.nextframe.schedulereservationticketing.entity.ticketing.Ticket;
import wisoft.nextframe.schedulereservationticketing.service.ticketing.TicketService;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

	private final TicketService ticketService;

	@PostMapping
	public ResponseEntity<TicketIssueResponse> issue(@RequestBody TicketIssueRequest request) {
		Ticket ticket = ticketService.issueByReservation(request.reservationId());
		TicketIssueResponse response = TicketIssueResponse.from(ticket); // 변환
		return ResponseEntity.ok(response);
	}
}