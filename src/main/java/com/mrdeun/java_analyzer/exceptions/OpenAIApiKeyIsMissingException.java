package com.mrdeun.java_analyzer.exceptions;

public class OpenAIApiKeyIsMissingException extends RuntimeException {
    public OpenAIApiKeyIsMissingException(){
        super("OPENAI_API_KEY enviromental variable is missing - Please provide it!");
    }
}
