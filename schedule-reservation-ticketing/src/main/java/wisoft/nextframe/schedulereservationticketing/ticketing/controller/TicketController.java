package wisoft.nextframe.schedulereservationticketing.ticketing.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.ticketing.controller.dto.TicketIssueRequest;
import wisoft.nextframe.schedulereservationticketing.ticketing.controller.dto.TicketResponse;
import wisoft.nextframe.schedulereservationticketing.ticketing.entity.Ticket;
import wisoft.nextframe.schedulereservationticketing.ticketing.service.TicketService;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

	private final TicketService ticketService;

	@PostMapping
	public ResponseEntity<TicketResponse> issue(@RequestBody TicketIssueRequest req) {
		Ticket ticket = ticketService.issueByReservation(req.reservationId());
		TicketResponse res = TicketResponse.from(ticket); // 변환
		return ResponseEntity.ok(res);
	}
}
