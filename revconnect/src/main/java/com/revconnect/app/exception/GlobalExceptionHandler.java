package com.revconnect.app.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ModelAndView handleGlobalException(Exception ex) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("error", "An unexpected error occurred: " + ex.getMessage());
        mav.setViewName("error"); // Assumes we will create a generic error.html template
        return mav;
    }

    @ExceptionHandler(RuntimeException.class)
    public ModelAndView handleRuntimeException(RuntimeException ex) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("error", ex.getMessage());
        mav.setViewName("error");
        return mav;
    }
}
