package com.jason.server.container;

import java.net.URLClassLoader;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jason.server.connector.Bootstrap;
import com.jason.server.connector.HttpProcessor;
import com.jason.server.connector.HttpProcessor.ActionCode;
import com.jason.server.connector.MyServletRequest;
import com.jason.server.connector.MyServletResponse;

/**
 * Process request for 
 * @author Administrator
 *
 */
public class ServletProcessor
{
	private static final Logger log = LogManager.getLogger(ServletProcessor.class);
	
	private HttpProcessor httpProcessor;//use for callback
	public HttpProcessor getHttpProcessor() { return httpProcessor; }
	public void setHttpProcessor(HttpProcessor httpProcessor) { 
		this.httpProcessor = httpProcessor; 
	}
	
	public ServletProcessor(HttpProcessor httpProcessor)
	{
		this.httpProcessor = httpProcessor;
	}
	
	public void process(MyServletRequest request,MyServletResponse response)
	{
		String uri = request.getRequestURI();
		String servletName = uri.substring(uri.lastIndexOf("/")+1);//get the servlet name
		URLClassLoader classLoader = Bootstrap.getServletLoader();//Bootstrap is static in this application
		
		Class<?> myClass = null;
		try
		{
			myClass = classLoader.loadClass(servletName);
		}
		catch(ClassNotFoundException e)
		{
			log.warn("cant find class");
			response.setError(true);
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			httpProcessor.action(ActionCode.COMMIT);
			return;
		}
		Servlet servlet = null;
		try {
			//wrapper object
			HttpServletRequest httpServletRequest =new HttpServletRequestWrapper(request);
			HttpServletResponse httpServletResponse = new HttpServletResponseWrapper(response);
			servlet = (Servlet)myClass.newInstance();
			servlet.service((HttpServletRequest)httpServletRequest,(HttpServletResponse)httpServletResponse);
		} 	catch (Exception e) {
			if(e instanceof ServletException){
				log.error("error while calling servlet",e);
			} else {
				log.error("error during instantiation",e);
			}
			//send error page
			response.setError(true);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			httpProcessor.action(ActionCode.COMMIT);
			return;
		}
	}
	
	public void recycle()
	{
		//Nothing to do for now 
	}
}
			
			
			
		
		