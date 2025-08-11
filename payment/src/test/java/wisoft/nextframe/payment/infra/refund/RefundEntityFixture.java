package wisoft.nextframe.payment.infra.refund;

import java.time.LocalDateTime;
import java.util.UUID;

import wisoft.nextframe.payment.common.Money;
import wisoft.nextframe.payment.refund.Refund;
import wisoft.nextframe.payment.refund.RefundEntity;
import wisoft.nextframe.payment.refund.RefundId;
import wisoft.nextframe.payment.refund.RefundPolicyStatus;
import wisoft.nextframe.payment.refund.RefundStatus;

public class RefundEntityFixture {

	public static final UUID DEFAULT_REFUND_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
	public static final UUID DEFAULT_PAYMENT_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
	public static final int DEFAULT_REFUND_AMOUNT = 12000;
	public static final String DEFAULT_STATUS = "REQUESTED";
	public static final String DEFAULT_POLICY = "REFUND_80_PERCENT";
	public static final String DEFAULT_REASON = "관람 불가 사유";
	public static final LocalDateTime DEFAULT_REQUESTED_AT = LocalDateTime.of(2025, 7, 31, 12, 0);
	public static final LocalDateTime DEFAULT_COMPLETED_AT = LocalDateTime.of(2025, 8, 1, 9, 30);

	public static RefundEntity sampleEntity() {
		return RefundEntity.builder()
			.id(DEFAULT_REFUND_ID)
			.paymentId(DEFAULT_PAYMENT_ID)
			.refundAmount(DEFAULT_REFUND_AMOUNT)
			.status(DEFAULT_STATUS)
			.refundPolicy(DEFAULT_POLICY)
			.reason(DEFAULT_REASON)
			.requestedAt(DEFAULT_REQUESTED_AT)
			.completedAt(DEFAULT_COMPLETED_AT)
			.build();
	}

	public static Refund sampleDomain() {
		return Refund.reconstruct(
			RefundId.of(DEFAULT_REFUND_ID),
			Money.of(DEFAULT_REFUND_AMOUNT),
			RefundStatus.valueOf(DEFAULT_STATUS),
			RefundPolicyStatus.valueOf(DEFAULT_POLICY),
			DEFAULT_REQUESTED_AT,
			DEFAULT_COMPLETED_AT
		);
	}
}
