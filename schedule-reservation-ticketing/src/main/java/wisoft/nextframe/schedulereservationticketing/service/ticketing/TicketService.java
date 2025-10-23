package wisoft.nextframe.schedulereservationticketing.service.ticketing;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.schedulereservationticketing.common.exception.ErrorCode;
import wisoft.nextframe.schedulereservationticketing.dto.ticketing.TicketInfoResponse;
import wisoft.nextframe.schedulereservationticketing.entity.reservation.Reservation;
import wisoft.nextframe.schedulereservationticketing.entity.ticketing.Ticket;
import wisoft.nextframe.schedulereservationticketing.common.exception.DomainException;
import wisoft.nextframe.schedulereservationticketing.repository.reservation.ReservationRepository;
import wisoft.nextframe.schedulereservationticketing.repository.ticketing.TicketRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

	private final TicketRepository ticketRepository;
	private final ReservationRepository reservationRepository;
	private final TicketSender ticketSender;

	/**
	 * 예약 기반 티켓 발급
	 * - 중복 발급 방지 멱등성 보장
	 */
	@Transactional
	public Ticket issueByReservation(UUID reservationId) {
		// 1. Reservation 조회
		Reservation reservation = reservationRepository.findById(reservationId)
			.orElseThrow(() -> new DomainException(ErrorCode.RESERVATION_NOT_FOUND));

		// 2. 중복 발급 체크
		ticketRepository.findByReservationId(reservation.getId())
			.ifPresent(existing -> {
				throw new DomainException(ErrorCode.TICKET_ALREADY_ISSUED);
			});

		// 3. 티켓 발급
		Ticket ticket = Ticket.issue(reservation);
		ticketRepository.saveAndFlush(ticket);

		TicketInfoResponse ticketInfo = ticketRepository.findTicketInfoById(ticket.getId())
			.orElseThrow(() -> new DomainException(ErrorCode.TICKET_NOT_FOUND));

		ticketSender.send(ticketInfo, reservation.getUser().getEmail());

		return ticket;

	}
}