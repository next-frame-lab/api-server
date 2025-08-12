// payment 모듈 - 구현(비공개)
package wisoft.nextframe.payment.application.payment;

import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
class PaymentFacadeImpl implements PaymentFacade {

	@Override
	public boolean isApproved(UUID paymentId) {
		return false;
	}

	@Override
	public boolean isApprovedByReservation(UUID reservationId) {
		return false;
	}
}
