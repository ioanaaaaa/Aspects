package com.example.home.services.observable;

import com.example.home.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Observable;
import java.util.Observer;

@Component
public class ManagerObserver implements Observer {
    private String managerEmail;
    private TaskObservableModel taskObservable;
    private EmailService emailService;

    @Autowired
    public ManagerObserver(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void update(Observable o, Object arg) {
        this.taskObservable = (TaskObservableModel) arg;
        sendEmailNotification();
    }

    public void setEmail(String email){
        this.managerEmail = email;
    }

    public void sendEmailNotification(){
        this.emailService.sendNotificationEmailToManager(this.managerEmail, this.taskObservable);
    }
}
