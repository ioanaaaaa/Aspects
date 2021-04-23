package com.example.home.services.states;

import com.example.home.models.Task;

public class CompletedState implements TaskState{
    @Override
    public void next(Task task) {
        //no other state
    }

    @Override
    public Task.Status getStatus() {
        return Task.Status.COMPLETED;
    }

    @Override
    public String toString(){
        return getStatus().name();
    }
}
