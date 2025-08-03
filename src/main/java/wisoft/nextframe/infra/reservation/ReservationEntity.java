package wisoft.nextframe.infra.reservation;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;

@Getter
@Entity
public class ReservationEntity {

	@Id
	private UUID id;
}
