package com.example.home.dtos.tasks;

import com.example.home.dtos.GroupDto;
import com.example.home.dtos.user.UserDto;
import com.example.home.models.Task;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskModelDto extends TaskSimpleDto {
    private List<UserDto> users;
    private List<GroupDto> groups;
    private Task.Status status;
    private UserDto claimedBy;
    private String state;

    public TaskModelDto(Task task) {
        this.setId(task.getId());
        this.setContent(task.getContent());
        this.setTitle(task.getTitle());
        this.setStatus(task.getStatus());
        this.setCategory(task.getCategory());
        this.setState(null != task.getState() ? task.getState().toString() : null);
        if (null != task.getClaimedBy()) {
            this.setClaimedBy(new UserDto(task.getClaimedByUser(), false));
        }
    }
}
