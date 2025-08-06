package wisoft.nextframe.payment.infra.payment;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import wisoft.nextframe.payment.domain.payment.PaymentStatus;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "payments")
public class PaymentEntity {

	@Id
	@GeneratedValue
	@Column(name = "id", nullable = false)
	private UUID id;

	@Column(name = "reservation_id", nullable = false, unique = true)
	private UUID reservationId;

	@Column(name = "total_amount")
	private Integer totalAmount;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private PaymentStatus status;

	@Column(name = "requested_at")
	private LocalDateTime requestedAt;

	@Column(name = "payment_method")
	private String paymentMethod;

	public PaymentEntity(
		UUID id,
		UUID reservationId,
		Integer totalAmount,
		PaymentStatus status,
		LocalDateTime requestedAt,
		String paymentMethod) {
		this.id = id;
		this.reservationId = reservationId;
		this.totalAmount = totalAmount;
		this.status = status;
		this.requestedAt = requestedAt;
		this.paymentMethod = paymentMethod;
	}

}
