package wisoft.nextframe.payment.infra.refund;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import wisoft.nextframe.payment.application.refund.port.output.RefundRepository;
import wisoft.nextframe.payment.domain.refund.Refund;

@Repository
@RequiredArgsConstructor
public class RefundRepositoryImpl implements RefundRepository {

	private final JpaRefundRepository jpaRefundRepository;
	private final RefundMapper refundMapper;

	@Override
	public Refund save(Refund refund, UUID paymentId, String reason) {
		RefundEntity entity = refundMapper.toEntity(refund, paymentId, reason);
		RefundEntity saved = jpaRefundRepository.save(entity);
		return refundMapper.toDomain(saved);
	}

	@Override
	public Optional<Refund> findByPaymentId(UUID paymentId) {
		return jpaRefundRepository.findByPaymentId(paymentId)
			.map(refundMapper::toDomain);
	}
}
