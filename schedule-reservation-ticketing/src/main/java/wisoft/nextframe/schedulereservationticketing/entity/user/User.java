package wisoft.nextframe.schedulereservationticketing.entity.user;

import java.time.LocalDate;
import java.time.Period;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
	private UUID id;

	private String name;

	private String email;

	@Column(name = "birth_date")
	private LocalDate birthDate;

	@Column(name = "phone_number")
	private String phoneNumber;

	@Column(name = "face_id")
	private String faceId;

	@Column(name = "image_url")
	private String imageUrl;

	@Column(name = "provider")
	private String provider;

	public boolean isAdult() {
		return Period.between(birthDate, LocalDate.now()).getYears() >= 19;
	}

	public void updateProfile(String name, String imageUrl) {
		this.name = name;
		this.imageUrl = imageUrl;
	}
}
