package com.example.demo.controller;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.example.demo.service.UserService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class AlgoCalculationHandler implements RequestStreamHandler {

    private final UserService userService;

    // Constructor with parameter for dependency injection
    public AlgoCalculationHandler(UserService userService) {
        this.userService = userService;
    }

    // Public zero-argument constructor required by AWS Lambda
    public AlgoCalculationHandler() {
        this.userService = new UserService(); // Create a default UserService instance
    }

    // This is the entry point method of your Lambda function
    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        userService.calculateAllUsersSimilarityAndSaveAfterDay2();
        return;
    }
}

