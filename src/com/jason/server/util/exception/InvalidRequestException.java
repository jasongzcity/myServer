package com.jason.server.util.exception;

/**
 * symbolize the exceptions of request
 * @author lwz
 * @since 2016-8-16 
 */
@SuppressWarnings("serial")
public class InvalidRequestException extends Exception 
{
    
    public InvalidRequestException(String message)
    {
        super(message);
    }
    
    public InvalidRequestException()
    {
        super();
    }
}
