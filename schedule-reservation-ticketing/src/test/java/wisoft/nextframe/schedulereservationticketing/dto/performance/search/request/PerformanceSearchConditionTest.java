package wisoft.nextframe.schedulereservationticketing.dto.performance.search.request;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class PerformanceSearchConditionTest {

	private final Pageable pageable = PageRequest.of(0, 32);

	@Nested
	@DisplayName("keyword 정규화 테스트")
	class KeywordNormalizationTest {

		@Test
		@DisplayName("null 키워드는 그대로 null을 유지한다")
		void nullKeyword_remainsNull() {
			// when
			PerformanceSearchCondition condition = PerformanceSearchCondition.of(null, null, pageable);

			// then
			assertThat(condition.keyword()).isNull();
		}

		@Test
		@DisplayName("빈 문자열 키워드는 null로 변환된다")
		void emptyKeyword_becomesNull() {
			// when
			PerformanceSearchCondition condition = PerformanceSearchCondition.of("", null, pageable);

			// then
			assertThat(condition.keyword()).isNull();
		}

		@Test
		@DisplayName("공백만 있는 키워드는 null로 변환된다")
		void blankKeyword_becomesNull() {
			// when
			PerformanceSearchCondition condition = PerformanceSearchCondition.of("   ", null, pageable);

			// then
			assertThat(condition.keyword()).isNull();
		}

		@Test
		@DisplayName("키워드 앞뒤 공백이 제거된다")
		void keyword_isStripped() {
			// when
			PerformanceSearchCondition condition = PerformanceSearchCondition.of("  햄릿  ", null, pageable);

			// then
			assertThat(condition.keyword()).isEqualTo("햄릿");
		}

		@Test
		@DisplayName("정상 키워드는 그대로 유지된다")
		void normalKeyword_remainsAsIs() {
			// when
			PerformanceSearchCondition condition = PerformanceSearchCondition.of("오페라의 유령", null, pageable);

			// then
			assertThat(condition.keyword()).isEqualTo("오페라의 유령");
		}
	}

	@Nested
	@DisplayName("sort 기본값 테스트")
	class SortDefaultTest {

		@Test
		@DisplayName("sort가 null이면 LATEST로 기본 설정된다")
		void nullSort_defaultsToLatest() {
			// when
			PerformanceSearchCondition condition = PerformanceSearchCondition.of("햄릿", null, pageable);

			// then
			assertThat(condition.sort()).isEqualTo(PerformanceSearchSort.LATEST);
		}

		@Test
		@DisplayName("sort가 지정되면 해당 값이 유지된다")
		void specifiedSort_remainsAsIs() {
			// when
			PerformanceSearchCondition condition = PerformanceSearchCondition.of("햄릿", PerformanceSearchSort.HIT_DESC, pageable);

			// then
			assertThat(condition.sort()).isEqualTo(PerformanceSearchSort.HIT_DESC);
		}
	}

	@Test
	@DisplayName("pageable이 그대로 전달된다")
	void pageable_isPassedThrough() {
		// given
		Pageable customPageable = PageRequest.of(2, 16);

		// when
		PerformanceSearchCondition condition = PerformanceSearchCondition.of("햄릿", PerformanceSearchSort.DATE_ASC, customPageable);

		// then
		assertThat(condition.pageable().getPageNumber()).isEqualTo(2);
		assertThat(condition.pageable().getPageSize()).isEqualTo(16);
	}
}
