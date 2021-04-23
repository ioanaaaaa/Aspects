package com.example.home.helpers.email.templates;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@EqualsAndHashCode
@Component
public class AssignTaskTemplateEmail implements Template {
    private static AssignTaskTemplateEmail instance = null;
    private String template;

    private AssignTaskTemplateEmail() {
        this.template = getResourceTemplate("C:\\Users\\ioana\\IdeaProjects\\RelovutBackend\\src\\main\\resources\\templates\\assign-template.html");
    }

    public static AssignTaskTemplateEmail getInstance() {
        if (instance == null)
            instance = new AssignTaskTemplateEmail();

        return instance;

    }
}
