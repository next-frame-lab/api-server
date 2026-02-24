package wisoft.nextframe.payment.infra.refund;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import wisoft.nextframe.payment.application.refund.port.output.RefundRepository;
import wisoft.nextframe.payment.domain.refund.Refund;
import wisoft.nextframe.payment.infra.payment.JpaPaymentRepository;
import wisoft.nextframe.payment.infra.payment.PaymentEntity;

@Repository
@RequiredArgsConstructor
public class RefundRepositoryImpl implements RefundRepository {

	private final JpaRefundRepository jpaRefundRepository;
	private final JpaPaymentRepository jpaPaymentRepository;
	private final RefundMapper refundMapper;

	@Override
	public Refund save(Refund refund, UUID paymentId, String reason) {
		PaymentEntity paymentRef = jpaPaymentRepository.getReferenceById(paymentId);
		RefundEntity entity = refundMapper.toEntity(refund, paymentRef, reason);
		RefundEntity saved = jpaRefundRepository.save(entity);
		return refundMapper.toDomain(saved);
	}

	@Override
	public Optional<Refund> findByPaymentId(UUID paymentId) {
		return jpaRefundRepository.findByPayment_Id(paymentId)
			.map(refundMapper::toDomain);
	}
}
