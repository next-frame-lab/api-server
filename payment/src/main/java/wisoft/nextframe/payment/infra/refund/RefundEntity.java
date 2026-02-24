package wisoft.nextframe.payment.infra.refund;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import wisoft.nextframe.payment.infra.payment.PaymentEntity;

@Entity
@Table(name = "refunds")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RefundEntity {

	@Id
	private UUID id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_id", nullable = false)
	private PaymentEntity payment;

	@Column(name = "refund_amount", nullable = false)
	private Integer refundAmount;

	@Column(name = "status", nullable = false)
	private String status;

	@Column(name = "reason")
	private String reason;

	@Column(name = "refund_policy")
	private String refundPolicy;

	@Column(name = "requested_at")
	private LocalDateTime requestedAt;

	@Column(name = "completed_at")
	private LocalDateTime completedAt;
}
