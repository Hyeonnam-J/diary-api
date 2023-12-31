package com.hn.api.diary.controller;

import com.hn.api.diary.exception.MyException;
import com.hn.api.diary.response.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class ExceptionController {

    @ResponseBody
    @ExceptionHandler(MyException.class)
    public ResponseEntity<ErrorResponse> DiaryExceptionHandler(MyException e){

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(e.getStatus())
                .message(e.getMessage())
                .build();
        int status = e.getStatus();

        ResponseEntity<ErrorResponse> responseEntity
                = ResponseEntity.status(status).body(errorResponse);

        return responseEntity;
    }

}
