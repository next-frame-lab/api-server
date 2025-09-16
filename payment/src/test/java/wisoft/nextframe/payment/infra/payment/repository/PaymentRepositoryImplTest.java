package wisoft.nextframe.payment.infra.payment.repository;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import wisoft.nextframe.payment.domain.fixture.PaymentEntityFixture;
import wisoft.nextframe.payment.domain.payment.Payment;
import wisoft.nextframe.payment.domain.payment.PaymentId;
import wisoft.nextframe.payment.infra.payment.JpaPaymentRepository;
import wisoft.nextframe.payment.infra.payment.PaymentEntity;
import wisoft.nextframe.payment.infra.payment.PaymentMapper;
import wisoft.nextframe.payment.infra.payment.PaymentRepositoryImpl;

@ExtendWith(MockitoExtension.class)
class PaymentRepositoryImplTest {

	@Mock
	JpaPaymentRepository jpaPaymentRepository;

	@Mock
	PaymentMapper paymentMapper;

	@InjectMocks
	PaymentRepositoryImpl paymentRepository;

	@Test
	void findById_should_return_payment_when_found() {
		// given
		UUID uuid = PaymentEntityFixture.DEFAULT_PAYMENT_ID;
		PaymentId id = PaymentId.of(uuid);
		PaymentEntity entity = PaymentEntityFixture.sampleEntity();
		Payment domain = PaymentEntityFixture.sampleDomain();

		given(jpaPaymentRepository.findById(uuid)).willReturn(Optional.of(entity));
		given(paymentMapper.toDomain(entity)).willReturn(domain);

		// when
		Optional<Payment> result = paymentRepository.findById(id);

		// then
		assertThat(result).isPresent();
		assertThat(result.get()).isEqualTo(domain);
	}

	@Test
	void findById_should_return_empty_when_not_found() {
		// given
		UUID uuid = UUID.randomUUID();
		PaymentId id = PaymentId.of(uuid);

		given(jpaPaymentRepository.findById(uuid)).willReturn(Optional.empty());

		// when
		Optional<Payment> result = paymentRepository.findById(id);

		// then
		assertThat(result).isEmpty();
	}

	@Test
	void save_should_persist_and_return_mapped_payment() {
		// given
		Payment domain = PaymentEntityFixture.sampleDomain();
		PaymentEntity entity = PaymentEntityFixture.sampleEntity();

		given(paymentMapper.toEntity(domain)).willReturn(entity);
		given(jpaPaymentRepository.save(entity)).willReturn(entity);
		given(paymentMapper.toDomain(entity)).willReturn(domain);

		// when
		Payment result = paymentRepository.save(domain);

		// then
		assertThat(result).isEqualTo(domain);
	}

}
