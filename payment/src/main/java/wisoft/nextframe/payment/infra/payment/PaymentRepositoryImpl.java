package wisoft.nextframe.payment.infra.payment;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import wisoft.nextframe.payment.application.payment.port.output.PaymentRepository;
import wisoft.nextframe.payment.domain.payment.Payment;
import wisoft.nextframe.payment.domain.payment.PaymentId;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

	private final JpaPaymentRepository jpaPaymentRepository;
	private final PaymentMapper paymentMapper;

	@Override
	public Payment save(Payment payment) {
		var entity = paymentMapper.toEntity(payment);
		var save = jpaPaymentRepository.save(entity);
		return paymentMapper.toDomain(save);
	}

	@Override
	public Optional<Payment> findById(PaymentId id) {
		return jpaPaymentRepository.findById(id.getValue())
			.map(paymentMapper::toDomain);
	}
}
