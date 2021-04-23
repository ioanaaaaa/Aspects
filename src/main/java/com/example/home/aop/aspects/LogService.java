package com.example.home.aop.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect
@Component
public class LogService {
    private long startTime;

    @Pointcut("execution (* com.example.home.services..*(..))")
    public void serviceMethodPointcut() {
    }

    @Before("serviceMethodPointcut()")
    public void executeBeforeServiceMethod() {
        startTime = System.currentTimeMillis();
    }

    @After("serviceMethodPointcut()")
    public void executeAfterServiceMethod(JoinPoint jp) {
        StringBuffer enterMethod = new StringBuffer();

        enterMethod.append("Entering in method ");
        enterMethod.append(jp.getSignature().getName());
        enterMethod.append("(");

        String args = Arrays.stream(jp.getArgs())
                .map(Object::toString)
                .collect(Collectors.joining(", "));
        enterMethod.append(args);
        enterMethod.append(")");

        System.out.println(enterMethod);

        StringBuffer exitMethod = new StringBuffer();
        exitMethod.append("Exit method ");
        exitMethod.append(jp.getSignature().getName());

        System.out.println(exitMethod);

        long endTime = System.currentTimeMillis() - startTime;
        System.out.println("Execution time is : " + endTime);
    }
}
