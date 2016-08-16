package com.jason.server.util.exception;

/**
 * symbolize the exceptions of response
 * @author lwz
 * @since 2016-8-16 
 */
public class InvalidResponseException extends Exception 
{
	private static final long serialVersionUID = 2322660625756341780L;

	public InvalidResponseException()
	{
		super();
	}
	
	public InvalidResponseException(String message)
	{
		super(message);
	}
}
