package com.example.home.controllers;

import com.example.home.dtos.user.RegisterUserDto;
import com.example.home.dtos.user.UserDto;
import com.example.home.models.User;
import com.example.home.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public String register(@Validated @RequestBody RegisterUserDto registerUserDto) {
        return userService.registerUser(registerUserDto);
    }

    @GetMapping("/current")
    public User getCurrentUser(Principal principal) {
        return userService.getByEmail(principal.getName());
    }

    @GetMapping("")
    public List<UserDto> searchUsers(@RequestParam(value = "searchTerm", required = false) String searchTerm){
        return UserDto.toDtos(userService.searchUsers(searchTerm));
    }

}
