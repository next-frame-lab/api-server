package wisoft.nextframe.payment.infra.persistence;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import wisoft.nextframe.payment.domain.refund.Refund;
import wisoft.nextframe.payment.infra.persistence.jpa.RefundEntity;
import wisoft.nextframe.payment.infra.persistence.jpa.RefundMapper;

public class RefundMapperTest {

	private final RefundMapper mapper = new RefundMapper();

	@Test
	void toDomain_정상_매핑() {
		// given
		RefundEntity entity = RefundEntityFixture.sampleEntity();

		// when
		Refund domain = mapper.toDomain(entity);

		// then
		assertThat(domain.getRefundId().getValue()).isEqualTo(entity.getId());
		assertThat(domain.getRefundedAmount().getValue().intValue()).isEqualTo(entity.getRefundAmount());
		assertThat(domain.getStatus().name()).isEqualTo(entity.getStatus());
		assertThat(domain.getPolicyStatus().name()).isEqualTo(entity.getRefundPolicy());
		assertThat(domain.getRequestedAt()).isEqualTo(entity.getRequestedAt());
		assertThat(domain.getCompletedAt()).isEqualTo(entity.getCompletedAt());
	}

	@Test
	void toEntity_정상_매핑() {
		// given
		Refund domain = RefundEntityFixture.sampleDomain();
		UUID paymentId = RefundEntityFixture.DEFAULT_PAYMENT_ID;
		String reason = RefundEntityFixture.DEFAULT_REASON;

		// when
		RefundEntity entity = mapper.toEntity(domain, paymentId, reason);

		// then
		assertThat(entity.getId()).isEqualTo(domain.getRefundId().getValue());
		assertThat(entity.getRefundAmount()).isEqualTo(domain.getRefundedAmount().getValue().intValue());
		assertThat(entity.getStatus()).isEqualTo(domain.getStatus().name());
		assertThat(entity.getRefundPolicy()).isEqualTo(domain.getPolicyStatus().name());
		assertThat(entity.getRequestedAt()).isEqualTo(domain.getRequestedAt());
		assertThat(entity.getCompletedAt()).isEqualTo(domain.getCompletedAt());
		assertThat(entity.getPaymentId()).isEqualTo(paymentId);
		assertThat(entity.getReason()).isEqualTo(reason);
	}
}
