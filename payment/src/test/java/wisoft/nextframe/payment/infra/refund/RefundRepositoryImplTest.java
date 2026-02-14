package wisoft.nextframe.payment.infra.refund;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import wisoft.nextframe.payment.domain.refund.Refund;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefundRepositoryImpl 단위 테스트")
class RefundRepositoryImplTest {

	@Mock
	JpaRefundRepository jpaRefundRepository;

	@Mock
	RefundMapper refundMapper;

	@InjectMocks
	RefundRepositoryImpl refundRepository;

	@Test
	@DisplayName("환불 저장 시 엔티티로 변환하여 저장하고 도메인으로 반환한다")
	void save_shouldPersistAndReturnMappedRefund() {
		// given
		Refund domain = RefundEntityFixture.sampleDomain();
		UUID paymentId = RefundEntityFixture.DEFAULT_PAYMENT_ID;
		String reason = RefundEntityFixture.DEFAULT_REASON;
		RefundEntity entity = RefundEntityFixture.sampleEntity();

		given(refundMapper.toEntity(domain, paymentId, reason)).willReturn(entity);
		given(jpaRefundRepository.save(entity)).willReturn(entity);
		given(refundMapper.toDomain(entity)).willReturn(domain);

		// when
		Refund result = refundRepository.save(domain, paymentId, reason);

		// then
		assertThat(result).isEqualTo(domain);
		then(jpaRefundRepository).should().save(entity);
	}

	@Test
	@DisplayName("paymentId로 환불 조회 시 존재하면 도메인 객체를 반환한다")
	void findByPaymentId_shouldReturnRefund_whenFound() {
		// given
		UUID paymentId = RefundEntityFixture.DEFAULT_PAYMENT_ID;
		RefundEntity entity = RefundEntityFixture.sampleEntity();
		Refund domain = RefundEntityFixture.sampleDomain();

		given(jpaRefundRepository.findByPaymentId(paymentId)).willReturn(Optional.of(entity));
		given(refundMapper.toDomain(entity)).willReturn(domain);

		// when
		Optional<Refund> result = refundRepository.findByPaymentId(paymentId);

		// then
		assertThat(result).isPresent();
		assertThat(result.get()).isEqualTo(domain);
	}

	@Test
	@DisplayName("paymentId로 환불 조회 시 존재하지 않으면 빈 Optional을 반환한다")
	void findByPaymentId_shouldReturnEmpty_whenNotFound() {
		// given
		UUID paymentId = UUID.randomUUID();
		given(jpaRefundRepository.findByPaymentId(paymentId)).willReturn(Optional.empty());

		// when
		Optional<Refund> result = refundRepository.findByPaymentId(paymentId);

		// then
		assertThat(result).isEmpty();
	}
}
