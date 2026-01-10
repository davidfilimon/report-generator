package com.mycompany.report_generator.security;

public class LLMGenerationException extends  RuntimeException{
    public LLMGenerationException(String message) {
        super(message);
    }
}
