package wisoft.nextframe.payment.infra.persistence.jpa;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import wisoft.nextframe.payment.domain.PaymentStatus;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PaymentEntity {

	@Id
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

}
