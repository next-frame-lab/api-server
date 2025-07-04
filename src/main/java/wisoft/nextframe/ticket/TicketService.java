package wisoft.nextframe.ticket;

import wisoft.nextframe.payment.Payment;

public class TicketService {
	public Ticket issue(Payment payment) {
		if (payment == null) {
			throw new IllegalArgumentException("결제 정보(payment)는 null일 수 없습니다.");
		}

		if (!payment.isPaid()) {
			throw new IllegalStateException("결제 완료된 상태가 아닙니다.");
		}

		return Ticket.issueFrom(payment); // QR 생성, 유효성 등 내부 처리
	}
}
