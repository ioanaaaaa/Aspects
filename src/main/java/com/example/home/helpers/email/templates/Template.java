package com.example.home.helpers.email.templates;

import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public interface Template {

    @SneakyThrows
    default String getResourceTemplate(String resource) {
        // Register Email Template
        InputStream stream = new FileInputStream(resource);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        return reader.lines().collect(Collectors.joining("\n"));
    }
}
