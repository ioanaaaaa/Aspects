//package com.fmi.homemanagement.aop;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.web.client.RestClientException;
//import org.springframework.web.server.ResponseStatusException;
//
//public aspect ControllerException {
//    pointcut controllerMethod(): execution (* com.fmi.homemanagement.controllers..*(..));
//
//    after() throwing(Exception ex): controllerMethod() {
//        System.out.println(thisJoinPoint.getSignature() + " has thrown an exception with the message: " + ex.getMessage());
//        System.out.println(ex.getStackTrace());
//        throw new RestClientException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), ex.getCause());
//    }
//}
