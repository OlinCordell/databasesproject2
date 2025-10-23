package uga.menik.csx370.controllers;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import uga.menik.csx370.models.User;
import uga.menik.csx370.services.UserService;


@ControllerAdvice
public class GlobalControllerAdvice {
    
    private final UserService userService;

    public GlobalControllerAdvice(UserService userService) {
        this.userService = userService;
    }

    @ModelAttribute
    public User addLoggedInUserToModel() {
        return userService.getLoggedInUser();
    }
}
