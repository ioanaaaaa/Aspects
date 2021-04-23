package com.example.home.services.observable;

import com.example.home.models.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
public class TaskObservableModel {
    private Action actionTaken;
    private User userOnTask;
    private Long id;
    private String title;

    public enum Action{
        CLAIM, SUBMIT
    }

    public TaskObservableModel(Action actionTaken, User userOnTask, Long id, String title) {
        this.actionTaken = actionTaken;
        this.userOnTask = userOnTask;
        this.id = id;
        this.title = title;
    }
}
