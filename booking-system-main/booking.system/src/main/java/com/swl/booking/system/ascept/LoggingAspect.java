package com.swl.booking.system.ascept;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
// @Component  // Commented out to disable verbose logging
public class LoggingAspect {

	private final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

	// Define pointcut for all controller methods
	@Pointcut("execution(public * com.swl.booking.system.controller.*.*(..))")
	public void controllerMethods() {
		// Pointcut expression to match all methods in controllers
	} 
	
	// Commented out to reduce log verbosity
	// @Before("controllerMethods()")
	// public void logMethodEntry(JoinPoint joinPoint) { 
	//     String methodName = joinPoint.getSignature().getName();
	//     Object[] methodArgs = joinPoint.getArgs();
	//     logger.info("Method execution started: {} method with arguments: {}", methodName, methodArgs);
	// }
	

	// Commented out to reduce log verbosity
	// @After("controllerMethods()") 
	// public void logMethodExit(JoinPoint joinPoint) { 
	//     String methodName = joinPoint.getSignature().getName();
	//     Object[] methodArgs = joinPoint.getArgs();
	//     logger.info("Method execution finished: {} method with arguments: {}", methodName, methodArgs);
	// }

	// Commented out to reduce log verbosity
	// @AfterReturning(pointcut = "controllerMethods()", returning = "result")
	// public void logMethodReturn(Object result) {
	//	logger.info("Method executed successfully. Return value: " + result);
	// }

	// After throwing advice: Log exception if any
	@AfterThrowing(pointcut = "controllerMethods()", throwing = "exception")
	public void logMethodException(Exception exception) {
		logger.error("An exception occurred: " + exception.getMessage(), exception);
	}
}
