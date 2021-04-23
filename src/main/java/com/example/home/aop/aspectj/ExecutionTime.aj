package com.example.home.aop.aspectj;

import com.example.home.dtos.GroupDto;

import java.util.List;

public aspect ExecutionTime {
    private long startTimeExec;

    pointcut controllerMethodExecute(): execution (public java.util.List com.example.home.controllers.GroupController.*(..));
    
    List<GroupDto> around(): controllerMethodExecute(){
    	startTimeExec = System.nanoTime();
    	System.out.println("Entering in method:" + thisJoinPoint.getSignature().getName());
    	
    	List<GroupDto> result = proceed();
        
    	long endTimeExec = System.nanoTime();
        System.out.println("Execution time for method" + thisJoinPoint.getSignature().toShortString() + "= " + (endTimeExec - startTimeExec) / 1000000 + " msec." );
		
        StringBuffer resultFromAspect = new StringBuffer("Result from aspect:");
        
        for(GroupDto groupDto: result) {
			resultFromAspect.append( " " + groupDto.getName() + ", ");
		}
        
        System.out.println(resultFromAspect);
        return result;
    }
}
