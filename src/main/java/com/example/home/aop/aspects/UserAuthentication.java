package com.example.home.aop.aspects;

import com.example.home.dtos.LoginUserDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Aspect
public class UserAuthentication {

    @Around("execution( * com.example.home.config.security.ApiJWTAuthenticationFilter + .attemptAuthentication(..)) && args(req, res))")
    public void logAroundUserAuthentication(ProceedingJoinPoint joinPoint, HttpServletRequest req, HttpServletResponse res) throws Throwable {
        System.out.println("****Login Request payload is: " + new ObjectMapper().readValue(req.getInputStream(), LoginUserDto.class).toString());

        joinPoint.proceed();

        System.out.println("****Login response() : " + res.getStatus());
    }

    @Around("execution( * com.example.home.config.security.ApiJWTAuthenticationFilter.attemptAuthentication*(..)) && args(req, res))")
    public void loggAroundUserAuthentication(ProceedingJoinPoint joinPoint, HttpServletRequest req, HttpServletResponse res) throws Throwable {
        System.out.println("****Login Request payload is: " + new ObjectMapper().readValue(req.getInputStream(), LoginUserDto.class).toString());

        joinPoint.proceed();

        System.out.println("****Login response() : " + res.getStatus());
    }

    @Around("execution(* com.example.home.config.security.ApiJWTAuthenticationFilter + .attemptAuthentication*(..))")
    public void loggAroundUserAudthentication(ProceedingJoinPoint joinPoint) throws Throwable {
//        System.out.println("****Login Request payload is: " + new ObjectMapper().readValue(req.getInputStream(), LoginUserDto.class).toString());

        joinPoint.proceed();

//        System.out.println("****Login response() : " + res.getStatus());
    }
}
