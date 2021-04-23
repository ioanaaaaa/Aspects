package com.example.home.aop.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Aspect
@Component
public class ControllerException {
    @Pointcut("execution (* com.example.home.controllers..*(..))")
    public void controllerMethod() {
    }

    @AfterThrowing(pointcut = "controllerMethod()", throwing = "ex")
    public void throwAfterException(JoinPoint jp, Exception ex) {
        System.out.println(jp.getSignature() + " has thrown an exception with the message: " + ex.getMessage());
        System.out.println(ex.getStackTrace());
        throw new RestClientException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), ex.getCause());
    }
}
