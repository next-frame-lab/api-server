package wisoft.nextframe.schedulereservationticketing.common.aop.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

	private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

	// 1. Controller를 감시할 범위 설정
	@Pointcut("within(wisoft.nextframe.schedulereservationticketing.controller..*)")
	public void controllerPointcut() {}

	// 2. Service를 감시할 범위 설정
	@Pointcut("within(wisoft.nextframe.schedulereservationticketing.service..*)")
	public void servicePointcut() {}

	// 3. controllerPointcut 또는 servicePointcut에 해당하는 메소드를 감시
	@Around("controllerPointcut() || servicePointcut()")
	public Object logAction(ProceedingJoinPoint joinPoint) throws Throwable {

		// 메서드 실행 전 로그
		final String className = joinPoint.getTarget().getClass().getSimpleName();
		final String methodName = joinPoint.getSignature().getName();
		final Object[] args = joinPoint.getArgs();

		log.info("==> Method: {}.{}()", className, methodName);

		// 원래 메소드 실행
		long startTime = System.currentTimeMillis();
		Object result = joinPoint.proceed();
		long endTime = System.currentTimeMillis();
		long executionTime = endTime - startTime;

		// 메소드 실행 후 로그
		log.info("<== Method: {}.{}() | Execution Time: {}ms", className, methodName, executionTime);

		return result;
	}
}
