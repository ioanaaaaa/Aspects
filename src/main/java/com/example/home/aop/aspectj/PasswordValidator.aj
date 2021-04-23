package com.example.home.aop.aspectj;

import com.example.home.dtos.user.RegisterUserDto;
import com.example.home.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public aspect PasswordValidator {
	
//	@Pattern
//	String passwordPattern = '^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$';
	
	pointcut callRegisterUserMethod(RegisterUserDto registerUserDto, UserService userService) : execution(* com.example.home.services.UserService.registerUser(*)) && args(registerUserDto) && target(userService);

    before(RegisterUserDto registerUserDto, UserService userService): callRegisterUserMethod(registerUserDto, userService){
        if (registerUserDto.getPassword().length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "From Aspect: The password must be at least 8 characters long!");
        }
    }
   
}
	
