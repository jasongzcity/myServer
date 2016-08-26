package com.jason.server.connector;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jason.server.connector.HttpProcessor.ActionCode;
import com.jason.server.container.ServletProcessor;
import com.jason.server.container.StaticResourceProcessor;
import com.jason.server.util.exception.InvalidRequestException;
import com.jason.server.util.http.HttpRequestUtil;

/**
 * Adapter service the request & response.
 * Finds the mapping resource for current session
 * and prepare for servlet
 * @author lwz
 * @since 2016-8-26 
 *
 */
public class MyAdapter 
{
	private static final Logger log = LogManager.getLogger(MyAdapter.class);
	
	private HttpProcessor httpProcessor;
	public void setHttpProcessor(HttpProcessor httpProcessor){ this.httpProcessor = httpProcessor; }
	public HttpProcessor getHttpProcessor(){ return httpProcessor; }
	
	public MyAdapter(HttpProcessor httpProcessor)
	{
		this.httpProcessor = httpProcessor;
	}
	
	/**
	 * Core method.
	 * @param request
	 * @param response
	 */
	public void service(MyServletRequest request,MyServletResponse response)
	{
		try
		{
			HttpRequestUtil.parseRequestLine(request);
			HttpRequestUtil.parseHeaders(request);
		}
		catch(InvalidRequestException e)//Bad Request
		{
			log.warn("Received bad request",e);
			response.setError(true);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			httpProcessor.action(ActionCode.COMMIT);
			return;
		}

		//easy resource mapping ;-)
		if(request.getRequestURI().startsWith("/servlet/"))	//calling servlet
		{
			//TODO: make processors recycled to avoid GC
			ServletProcessor servletProcessor = new ServletProcessor();
			servletProcessor.process(request,response);
		}
		else
		{
			StaticResourceProcessor staticProcessor = new StaticResourceProcessor(httpProcessor);
			staticProcessor.process(request,response);
		}
	}
}
