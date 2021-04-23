package com.example.home.dtos.user;

import com.example.home.models.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.catalina.Manager;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto extends ManagerDto {
    private Long id;
    private String email;
    private String fullname;

    @Builder(builderMethodName = "userDtoBuilder")
    public UserDto(boolean isManager, Long id, String email, String fullname){
        super(isManager);
        this.email = email;
        this.fullname = fullname;
        this.id = id;

    }

    public UserDto(boolean isManager, Long id, String email){
        super(isManager);
        this.email = email;
        this.id = id;

    }

    public UserDto(User user, boolean isManager){
        this.id = user.getId();
        this.setManager(isManager);
        this.fullname = user.getFullname();
        this.email = user.getEmail();

    }

    public static List<UserDto> toDtos(Map<User, Boolean> users){
        return  users.entrySet().stream()
                .map(userMap -> new UserDto(userMap.getKey(), userMap.getValue()))
                .collect(Collectors.toList());

    }

    public UserDto(User user){
        this.id = user.getId();
        this.fullname = user.getFullname();
        this.email = user.getEmail();

    }

    public static List<UserDto> toDtos(List<User> users){
        return  users.stream()
                .map(user -> new UserDto(user))
                .collect(Collectors.toList());

    }
}
