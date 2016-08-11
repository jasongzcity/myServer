package com.jason.server.util.exception;

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
