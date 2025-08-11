package wisoft.nextframe.schedulereservationticketing.schedule.entity.performance.typeconverter;

import java.math.BigDecimal;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class BigDecimalIntegerConverter implements AttributeConverter<BigDecimal, Integer> {

	@Override
	public Integer convertToDatabaseColumn(BigDecimal attribute) {
		if (attribute == null) {
			return null;
		}

		try {
			return attribute.intValueExact();
		} catch (ArithmeticException ex) {
			throw new IllegalArgumentException("price must be a whole number that fits 32-bit integer.", ex);
		}
	}

	@Override
	public BigDecimal convertToEntityAttribute(Integer dbData) {
		return (dbData == null) ? null : BigDecimal.valueOf(dbData);
	}
}