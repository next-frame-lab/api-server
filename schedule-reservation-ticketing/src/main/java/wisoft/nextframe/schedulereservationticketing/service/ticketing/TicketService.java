package wisoft.nextframe.schedulereservationticketing.service.ticketing;

import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.schedulereservationticketing.entity.ticketing.ReservationId;
import wisoft.nextframe.schedulereservationticketing.entity.ticketing.Ticket;
import wisoft.nextframe.schedulereservationticketing.exception.ticketing.AlreadyIssuedException;
import wisoft.nextframe.schedulereservationticketing.repository.ticketing.TicketRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

	private final TicketRepository ticketRepository;

	/**
	 * 예약 기반 티켓 발급
	 * - 중복 발급 방지 멱등성 보장
	 */
	@Transactional
	public Ticket issueByReservation(UUID reservationId) {
		ReservationId reservationIdObj = ReservationId.of(reservationId);

		ticketRepository.findByReservationId(reservationId)
			.ifPresent(existing -> {
				throw new AlreadyIssuedException(reservationIdObj);
			});

		Ticket ticket = Ticket.issue(reservationId);

		// 2. 저장
		try {
			return ticketRepository.save(ticket);
		} catch (DataIntegrityViolationException e) {
			// 멀티 스레드 경쟁 시 유니크 제약으로 중복 저장 실패 가능
			log.warn("중복 발급 경쟁 감지 reservationId={}", reservationId);
			return ticketRepository.findByReservationId(reservationId)
				.orElseThrow(() -> e); // 정말 없으면 원인 재전파
		}
	}
}