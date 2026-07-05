package com.studygenie.lesson.contentgenerator;

public class ContentGeneratorException extends RuntimeException {

    public ContentGeneratorException(String message) {
        super(message);
    }

    public ContentGeneratorException(String message, Throwable cause) {
        super(message, cause);
    }
}
