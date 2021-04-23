package com.example.home.aop.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.List;

@Aspect
@Component
public class RegisterUserAndServicesReturningAdvice {
    @Pointcut("execution (public String com.example.home.services.UserService.registerUser(..))")
    public void userRegisterPointcut() {
    }

    @Pointcut("execution (public java.util.List com.example.home.services..*(..))")
    public void servicesMethodReturningPointcut() {
    }

    @AfterReturning(pointcut = "userRegisterPointcut()", returning = "returnObject")
    public void getJWTReturningAdvice(JoinPoint jp, String returnObject) {
        System.out.println("---Method : " + jp.getSignature().getName() + " returned the following jwt=" + returnObject);
    }

    @AfterReturning(pointcut = "servicesMethodReturningPointcut()", returning = "returnObject")
    public void getReturningAdvice(JoinPoint jp, List returnObject) {
        System.out.println("---Service Method : " + jp.getSignature().getName() + " returned the following list=" + returnObject);
    }
}
