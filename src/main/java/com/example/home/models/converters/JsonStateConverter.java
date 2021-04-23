package com.example.home.models.converters;

import com.example.home.models.Task;
import com.example.home.services.states.CompletedState;
import com.example.home.services.states.CreatedState;
import com.example.home.services.states.InProgressState;
import com.example.home.services.states.TaskState;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;

@Converter(autoApply = true)
public class JsonStateConverter implements AttributeConverter<TaskState, String> {

    @Override
    public String convertToDatabaseColumn(TaskState taskState) {
        if(null != taskState) {
            return taskState.toString();
        }
        return null;
    }

    @SneakyThrows
    @Override
    public TaskState convertToEntityAttribute(String dbData) {
        if(null == dbData){
            return null;
        }
        Task.Status status = Task.Status.valueOf(dbData);

        if(Task.Status.COMPLETED.equals(status)){
            return new CompletedState();
        }
        if(Task.Status.IN_PROGRESS.equals(status)){
            return new InProgressState();
        }
        if(Task.Status.CREATED.equals(status)){
            return new CreatedState();
        }
        return null;
    }
}
