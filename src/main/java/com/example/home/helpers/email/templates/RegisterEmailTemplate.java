package com.example.home.helpers.email.templates;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@EqualsAndHashCode
@Component
public class RegisterEmailTemplate implements Template {
    private static RegisterEmailTemplate instance = null;
    public String template;

    private RegisterEmailTemplate() {
        this.template = getResourceTemplate("C:\\Users\\ioana\\IdeaProjects\\RelovutBackend\\src\\main\\resources\\templates\\register-template.html");
    }

    public static RegisterEmailTemplate getInstance() {
        if (instance == null)
            instance = new RegisterEmailTemplate();

        return instance;

    }
}
