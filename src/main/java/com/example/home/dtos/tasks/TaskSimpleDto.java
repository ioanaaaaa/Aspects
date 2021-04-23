package com.example.home.dtos.tasks;

import com.example.home.models.Task;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class TaskSimpleDto {
    private Long id;
    @NotNull
    private String title;
    @NotNull
    private String content;
    @NotNull
    private Task.Category category;
}
