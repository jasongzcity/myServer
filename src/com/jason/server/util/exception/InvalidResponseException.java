package com.jason.server.util.exception;

/**
 * symbolize the exceptions of response
 * @author lwz
 * @since 2016-8-16 
 */
@SuppressWarnings("serial")
public class InvalidResponseException extends Exception 
{
    
    public InvalidResponseException()
    {
        super();
    }
    
    public InvalidResponseException(String message)
    {
        super(message);
    }
}
