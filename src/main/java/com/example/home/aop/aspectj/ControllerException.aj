package com.example.home.aop.aspectj;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientException;

import java.util.Arrays;

public aspect ControllerException {

    pointcut controllerMethod(): execution (* com.example.home.controllers..*(..));

    after() throwing(Exception ex): controllerMethod() {
        System.out.println(thisJoinPoint.getSignature() + " has thrown an exception with the message: " + ex.getMessage());
        System.out.println(Arrays.toString(ex.getStackTrace()));
        throw new RestClientException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), ex.getCause());
    }
}
