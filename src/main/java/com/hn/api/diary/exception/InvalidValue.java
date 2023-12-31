package com.hn.api.diary.exception;

public class InvalidValue extends MyException {

    private static final String MESSAGE = "invalid value";

    public InvalidValue() {
        super(MESSAGE);
    }

    @Override
    public int getStatus() {
        return 404;
    }
}
