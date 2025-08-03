package wisoft.nextframe.domain.seat;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(of = {"section", "row", "column"})
public class Seat {

	private final SeatId id;
	@Getter
	private final String section;
	private final int row;
	private final int column;
	@Getter
	private boolean isLocked = false;

	private Seat(SeatId id, String section, int row, int column) {
		this.id = id;
		this.section = section;
		this.row = row;
		this.column = column;
	}

	public static Seat create(String section, int row, int column) {
		return new Seat(SeatId.of(), section, row, column);
	}

	public void lock() {
		this.isLocked = true;
	}

	public void unlock() {
		this.isLocked = false;
	}
}
