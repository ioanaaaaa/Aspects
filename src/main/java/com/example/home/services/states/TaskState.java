package com.example.home.services.states;

import com.example.home.models.Task;

public interface TaskState {
    void next(Task task);
    Task.Status getStatus();
}
