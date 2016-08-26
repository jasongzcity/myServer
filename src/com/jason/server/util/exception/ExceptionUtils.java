package com.jason.server.util.exception;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Fast method for exceptions handling
 * @author lwz
 * @since 2016-8-26 
 */
public class ExceptionUtils 
{
	private static final Logger log = LogManager.getLogger(ExceptionUtils.class);
	
	/**
	 * Swallow normal exceptions, which sometimes 
	 * we have nothing to deal with it.
	 * eg.annoying IOException while closing streams.
	 * @param e
	 */
	public static void swallowException(Exception e)
	{
		if(log.isDebugEnabled())
		{
			log.debug(e);
		}
	}
	
	/**
	 * Swallow all throwable except those are fatal.
	 * To protect thread from dying of recoverable exceptions. 
	 * @param t
	 * @throws Error
	 */
	public static void swallowThrowable(Throwable t)throws Error
	{
		if(t instanceof Error)//serious,throw it 
		{
			log.error(t);
			throw (Error)t;
		}

		log.warn(t);
	}
	
	/**
	 * Transfer exceptions to runtime exception 
	 * and no need to worry about writing any annoying try-catch 
	 * block along the thread's stack.
	 * @param e
	 */
	public static void transRuntimeException(Exception e)
	{
		throw new RuntimeException(e.getMessage());
	}
}
