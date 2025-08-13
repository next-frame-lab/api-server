package wisoft.nextframe.schedulereservationticketing.entity.performance.typeconverter;

import java.time.Duration;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class DurationMinutesConverter implements AttributeConverter<Duration, Integer> {

	@Override
	public Integer convertToDatabaseColumn(Duration duration) {
		return (duration != null) ? (int) duration.toMinutes() : null;
	}

	@Override
	public Duration convertToEntityAttribute(Integer minutes) {
		return (minutes != null) ? Duration.ofMinutes(minutes) : null;
	}
}