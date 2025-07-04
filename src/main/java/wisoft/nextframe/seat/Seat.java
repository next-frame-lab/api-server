package wisoft.nextframe.seat;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(of = {"section", "row", "column"})
public class Seat {

	private final String section;
	private final String row;
	private final int column;
	private boolean isLocked = false;

	private Seat(String section, String row, int column) {
		this.section = section;
		this.row = row;
		this.column = column;
	}

	public static Seat create(String section, String row, int column) {
		return new Seat(section, row, column);
	}

	public boolean isLocked() {
		return isLocked;
	}

	public String getSection() {
		return section;
	}

	public void lock() {
		this.isLocked = true;
	}

	public void unlock() {
		this.isLocked = false;
	}
}
