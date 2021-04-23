package com.example.home.dtos.tasks;

import com.example.home.models.Task;
import lombok.Data;

@Data
public class TaskFilterDto extends IsMatchDto {
    private Task.Status status;
    private Long groupId;
    private Long userId;


}
