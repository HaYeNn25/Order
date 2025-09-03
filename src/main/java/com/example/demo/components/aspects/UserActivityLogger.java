package com.example.demo.components.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.logging.Logger;

@Component
@Aspect
public class UserActivityLogger {
    private Logger logger = Logger.getLogger(getClass().getName());

    //name pointcut
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)") //pointcut quanh @RestController
    public void controllerMethod() {}

    @Around("execution(* com.example.demo.controllers.UserController*(..))")
    public Object logUserActivity(ProceedingJoinPoint joinPoint) throws Throwable {
        //Ghi log trước khi thực hiện method
        String methodName = joinPoint.getSignature().getName();
        String remoteAddress = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest().getRemoteAddr();
        logger.info("User activity started: " + methodName + ", Ip: " + remoteAddress);
        // thực hiện method gốc
        Object result = joinPoint.proceed();
        //Ghi log trước khi thực hiện method
        logger.info("User activity finished: "+ methodName);
        return result;
    }
}
