package wisoft.nextframe.payment.presentation.refund.dto;

import java.util.UUID;

public record RefundApprovedData(
	UUID refundId,
	int refundAmount,
	String refundPolicy,
	String status
) {
}
