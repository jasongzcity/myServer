package com.jason.server.util.exception;

public class InvalidRequestException extends Exception 
{
	private static final long serialVersionUID = 6219875149693679058L;
	
	public InvalidRequestException(String message)
	{
		super(message);
	}
	
	public InvalidRequestException()
	{
		super();
	}
}
