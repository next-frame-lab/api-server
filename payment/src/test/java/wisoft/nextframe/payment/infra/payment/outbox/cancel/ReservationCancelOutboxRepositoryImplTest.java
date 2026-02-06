package wisoft.nextframe.payment.infra.payment.outbox.cancel;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

@ExtendWith(MockitoExtension.class)
class ReservationCancelOutboxRepositoryImplTest {

	@Mock
	private JpaReservationCancelOutboxRepository jpa;

	@Mock
	private Environment env;

	@InjectMocks
	private ReservationCancelOutboxRepositoryImpl repository;

	private final UUID reservationId = UUID.randomUUID();
	private final UUID paymentId = UUID.randomUUID();
	private final LocalDateTime now = LocalDateTime.now();

	@Test
	@DisplayName("기존 기록이 없으면 새로운 PENDING 레코드를 생성한다")
	void upsertPending_createNew() {
		// given
		given(jpa.findByReservationId(reservationId)).willReturn(Optional.empty());

		// when
		repository.upsertPending(paymentId, reservationId, "error", now);

		// then
		ArgumentCaptor<ReservationCancelOutboxEntity> captor = ArgumentCaptor.forClass(ReservationCancelOutboxEntity.class);
		verify(jpa).save(captor.capture());

		assertThat(captor.getValue().getStatus()).isEqualTo("PENDING");
		assertThat(captor.getValue().getReservationId()).isEqualTo(reservationId);
		assertThat(captor.getValue().getPaymentId()).isEqualTo(paymentId);
	}

	@Test
	@DisplayName("이미 PENDING 상태라면 에러 메시지와 시간을 업데이트한다")
	void upsertPending_updateExisting() {
		// given
		ReservationCancelOutboxEntity existing = new ReservationCancelOutboxEntity();
		existing.setStatus("PENDING");
		given(jpa.findByReservationId(reservationId)).willReturn(Optional.of(existing));

		// when
		repository.upsertPending(paymentId, reservationId, "new error", now);

		// then
		assertThat(existing.getLastError()).isEqualTo("new error");
		verify(jpa).save(existing);
	}

	@Test
	@DisplayName("이미 SUCCESS 상태인 레코드는 수정하지 않고 리턴한다")
	void upsertPending_ignoreIfSuccess() {
		// given
		ReservationCancelOutboxEntity existing = new ReservationCancelOutboxEntity();
		existing.setStatus("SUCCESS");
		given(jpa.findByReservationId(reservationId)).willReturn(Optional.of(existing));

		// when
		repository.upsertPending(paymentId, reservationId, "any error", now);

		// then
		verify(jpa, never()).save(any());
	}

	@Test
	@DisplayName("markSuccess 호출 시 상태를 SUCCESS로 변경한다")
	void markSuccess_updatesStatus() {
		// given
		ReservationCancelOutboxEntity existing = new ReservationCancelOutboxEntity();
		existing.setStatus("PENDING");
		given(jpa.findByReservationId(reservationId)).willReturn(Optional.of(existing));

		// when
		repository.markSuccess(reservationId, now);

		// then
		assertThat(existing.getStatus()).isEqualTo("SUCCESS");
		assertThat(existing.getUpdatedAt()).isEqualTo(now);
		verify(jpa).save(existing);
	}

	@Test
	@DisplayName("failAndBackoff 첫 번째 실패 시 5초 후 재시도로 설정한다")
	void failAndBackoff_firstRetry() {
		// given
		ReservationCancelOutboxEntity existing = new ReservationCancelOutboxEntity();
		existing.setStatus("PENDING");
		existing.setRetryCount(0);
		given(jpa.findByReservationId(reservationId)).willReturn(Optional.of(existing));

		// when
		repository.failAndBackoff(reservationId, "error", now);

		// then
		assertThat(existing.getRetryCount()).isEqualTo(1);
		assertThat(existing.getNextRetryAt()).isEqualTo(now.plusSeconds(5));
		verify(jpa).save(existing);
	}

	@Test
	@DisplayName("failAndBackoff 5번째 실패 시 FAILED 상태로 변경한다")
	void failAndBackoff_markFailed() {
		// given
		ReservationCancelOutboxEntity existing = new ReservationCancelOutboxEntity();
		existing.setStatus("PENDING");
		existing.setRetryCount(4);
		given(jpa.findByReservationId(reservationId)).willReturn(Optional.of(existing));

		// when
		repository.failAndBackoff(reservationId, "final error", now);

		// then
		assertThat(existing.getRetryCount()).isEqualTo(5);
		assertThat(existing.getStatus()).isEqualTo("FAILED");
		assertThat(existing.getNextRetryAt()).isNull();
		verify(jpa).save(existing);
	}
}
