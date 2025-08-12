package wisoft.nextframe.payment.persistence;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import wisoft.nextframe.payment.domain.payment.Payment;
import wisoft.nextframe.payment.infra.payment.PaymentEntity;
import wisoft.nextframe.payment.infra.payment.PaymentMapper;

public class PaymentMapperTest {

	private final PaymentMapper mapper = new PaymentMapper();


	@Test
	@DisplayName("도메인 Payment를 PaymentEntity로 변환한다")
	public void toEntity_shouldMapAllFields() {
		// given
		Payment payment = PaymentEntityFixture.sampleDomain();

		// when
		PaymentEntity entity = mapper.toEntity(payment);

		// then
		assertThat(entity.getId()).isEqualTo(payment.getId().getValue());
		assertThat(entity.getReservationId()).isEqualTo(payment.getReservationId().getValue());
		assertThat(entity.getTotalAmount()).isEqualTo(payment.getAmount().getValue().intValue());
		assertThat(entity.getStatus()).isEqualTo(payment.getStatus());
	}

	@Test
	@DisplayName("PaymentEntity를 도메인 Payment로 변환한다")
	public void toDomain_shouldMapAllFields() {
		// given
		PaymentEntity entity = PaymentEntityFixture.sampleEntity();

		// when
		Payment payment = mapper.toDomain(entity);

		// then
		assertThat(payment.getId().getValue()).isEqualTo(entity.getId());
		assertThat(payment.getReservationId().getValue()).isEqualTo(entity.getReservationId());
		assertThat(payment.getAmount().getValue().intValue()).isEqualTo(entity.getTotalAmount());
		assertThat(payment.getStatus()).isEqualTo(entity.getStatus());
	}
}
