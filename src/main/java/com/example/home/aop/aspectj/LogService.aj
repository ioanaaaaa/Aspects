//package com.fmi.homemanagement.aop;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.stream.Collectors;
//
//public aspect LogService {
//    private long startTime;
//
//    pointcut serviceMethodExecute(): execution (* com.fmi.homemanagement.services..*(..));
//
//    before(): serviceMethodExecute() {
//        startTime = System.currentTimeMillis();
//    }
//
//    after(): serviceMethodExecute() {
//        StringBuffer enterMethod = new StringBuffer();
//
//        enterMethod.append("Entering in method ");
//        enterMethod.append(thisJoinPoint.getSignature().getName());
//        enterMethod.append("(");
//
//        String args = null;
////        String args = Arrays.stream(thisJoinPoint.getArgs())
////                .map(Object::toString)
////                .collect(Collectors.joining(", "));
//        enterMethod.append(args);
//        enterMethod.append(")");
//
//        System.out.println(enterMethod);
//
//        StringBuffer exitMethod = new StringBuffer();
//        exitMethod.append("Exit method ");
//        exitMethod.append(thisJoinPoint.getSignature().getName());
//
//        System.out.println(exitMethod);
//
//        long endTime = System.currentTimeMillis() - startTime;
//        System.out.println("Execution time is : " + endTime);
//    }
//}
