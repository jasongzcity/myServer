package com.jason.server.util.exception;

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
