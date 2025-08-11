package wisoft.nextframe.schedulereservationticketing.common.mapper;

public interface EntityMapper<T, E> {
	T toDomain(E entity);

	E toEntity(T domain);
}
