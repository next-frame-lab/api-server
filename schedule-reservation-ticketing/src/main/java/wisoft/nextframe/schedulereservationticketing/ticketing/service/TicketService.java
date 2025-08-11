package wisoft.nextframe.schedulereservationticketing.ticketing.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.ticketing.entity.Ticket;
import wisoft.nextframe.schedulereservationticketing.ticketing.repository.TicketRepository;

@Service
@RequiredArgsConstructor
public class TicketService {

	private final TicketRepository ticketRepository;

	@Transactional
	public Ticket issueByReservation(UUID reservationId) {
		// // 1. 결제 승인 여부 검증
		// if (!paymentFacade.isApprovedByReservation(reservationId)) {
		// 	throw new IllegalStateException("결제가 완료되지 않은 예약입니다.");
		// }

		// 2. 티켓 발급
		Ticket ticket = Ticket.issue(reservationId);
		return ticketRepository.save(ticket);
	}
}