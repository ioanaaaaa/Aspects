package com.example.home.services.states;

import com.example.home.models.Task;

public class InProgressState implements TaskState{
    @Override
    public void next(Task task) {
        task.setState(new CompletedState());
    }

    @Override
    public Task.Status getStatus() {
        return Task.Status.IN_PROGRESS;
    }

    @Override
    public String toString(){
        return getStatus().name();
    }
}
