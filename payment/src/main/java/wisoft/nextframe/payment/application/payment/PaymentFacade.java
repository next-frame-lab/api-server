// payment 모듈 - 공개 API
package wisoft.nextframe.payment.application.payment;

import java.util.UUID;

public interface PaymentFacade {
	boolean isApproved(UUID paymentId);
	boolean isApprovedByReservation(UUID reservationId);
}
