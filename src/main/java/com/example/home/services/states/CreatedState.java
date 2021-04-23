package com.example.home.services.states;

import com.example.home.models.Task;

public class CreatedState implements TaskState {
    @Override
    public void next(Task task) {
        task.setState(new InProgressState());
    }

    @Override
    public Task.Status getStatus() {
        return Task.Status.CREATED;
    }

    @Override
    public String toString(){
        return getStatus().name();
    }
}
