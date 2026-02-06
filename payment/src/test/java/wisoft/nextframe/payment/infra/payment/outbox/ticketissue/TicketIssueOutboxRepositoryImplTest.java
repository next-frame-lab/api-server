package wisoft.nextframe.payment.infra.payment.outbox.ticketissue;

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
class TicketIssueOutboxRepositoryImplTest {

	@Mock
	private JpaTicketIssueOutboxRepository jpa;

	@Mock
	private Environment env;

	@InjectMocks
	private TicketIssueOutboxRepositoryImpl repository;

	private final UUID reservationId = UUID.randomUUID();
	private final UUID paymentId = UUID.randomUUID();
	private final LocalDateTime now = LocalDateTime.now();

	@Test
	@DisplayName("기존 기록이 없으면 새로운 PENDING 레코드를 생성한다")
	void upsertPending_createNew() {
		// given: DB에 데이터가 없음
		given(jpa.findByReservationId(reservationId)).willReturn(Optional.empty());

		// when
		repository.upsertPending(paymentId, reservationId, "error", now);

		// then: 새로운 엔티티가 PENDING 상태로 저장되는지 검증
		ArgumentCaptor<TicketIssueOutboxEntity> captor = ArgumentCaptor.forClass(TicketIssueOutboxEntity.class);
		verify(jpa).save(captor.capture());

		assertThat(captor.getValue().getStatus()).isEqualTo("PENDING");
		assertThat(captor.getValue().getReservationId()).isEqualTo(reservationId);
	}

	@Test
	@DisplayName("이미 PENDING 상태라면 에러 메시지와 시간을 업데이트한다")
	void upsertPending_updateExisting() {
		// given: 기존 PENDING 데이터 존재
		TicketIssueOutboxEntity existing = new TicketIssueOutboxEntity();
		existing.setStatus("PENDING");
		given(jpa.findByReservationId(reservationId)).willReturn(Optional.of(existing));

		// when
		repository.upsertPending(paymentId, reservationId, "new error", now);

		// then: lastError가 업데이트되었는지 확인
		assertThat(existing.getLastError()).isEqualTo("new error");
		verify(jpa).save(existing);
	}

	@Test
	@DisplayName("이미 SUCCESS 상태인 레코드는 수정하지 않고 리턴한다")
	void upsertPending_ignoreIfSuccess() {
		// given: 이미 SUCCESS인 데이터 존재
		TicketIssueOutboxEntity existing = new TicketIssueOutboxEntity();
		existing.setStatus("SUCCESS");
		given(jpa.findByReservationId(reservationId)).willReturn(Optional.of(existing));

		// when
		repository.upsertPending(paymentId, reservationId, "any error", now);

		// then: save()가 호출되지 않아야 함 (데이터 보호)
		verify(jpa, never()).save(any());
	}
}
