package wisoft.nextframe.infra.ticket;

import static org.assertj.core.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import wisoft.nextframe.domain.ticket.Ticket;
import wisoft.nextframe.util.TicketEntityFixture;

class TicketMapperTest {

	private final TicketMapper mapper = new TicketMapper();

	@Test
	void toDomain_매핑이_정상적으로_동작한다() {
		// given
		TicketEntity entity = TicketEntityFixture.sampleEntity();

		// when
		Ticket domain = mapper.toDomain(entity);

		// then
		assertThat(domain.getTicketId().getValue()).isEqualTo(entity.getId());
		assertThat(domain.getReservationId().getValue()).isEqualTo(entity.getReservationId());
		assertThat(domain.getQrCode()).isEqualTo(entity.getQrCode());
		assertThat(domain.getIssuedAt()).isEqualTo(entity.getIssuedAt());
	}

	@Test
	void toEntity_매핑이_정상적으로_동작한다() {
		// given
		Ticket domain = TicketEntityFixture.sampleDomain();
		UUID seatId = UUID.fromString("33333333-3333-3333-3333-333333333333");
		UUID scheduleId = UUID.fromString("44444444-4444-4444-4444-444444444444");

		// when
		TicketEntity entity = mapper.toEntity(domain, seatId, scheduleId);

		// then
		assertThat(entity.getId()).isEqualTo(domain.getTicketId().getValue());
		assertThat(entity.getReservationId()).isEqualTo(domain.getReservationId().getValue());
		assertThat(entity.getQrCode()).isEqualTo(domain.getQrCode());
		assertThat(entity.getIssuedAt()).isEqualTo(domain.getIssuedAt());
		assertThat(entity.getSeatId()).isEqualTo(seatId);
		assertThat(entity.getScheduleId()).isEqualTo(scheduleId);
		assertThat(entity.isUsed()).isFalse(); // 기본값 확인
	}
}
