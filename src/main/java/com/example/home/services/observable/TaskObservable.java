package com.example.home.services.observable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Observable;

public class TaskObservable extends Observable {
    private TaskObservableModel taskObservableModel;

    public void setTaskObservableModel(TaskObservableModel taskObservableModel) {
        this.taskObservableModel = taskObservableModel;
        setChanged();
        notifyObservers(taskObservableModel);
    }

}
