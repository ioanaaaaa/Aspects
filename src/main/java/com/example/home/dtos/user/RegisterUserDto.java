package com.example.home.dtos.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterUserDto {
    @NotNull
    @Email
    private String email;

    @NotNull
    @Length(min = 8, max = 64)
    private String password;

    @NotNull
    private String fullname;

}
