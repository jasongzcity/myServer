package com.jason.server.connector;

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import com.jason.server.container.ServletProcessor;
import com.jason.server.container.StaticResourceProcessor;
import com.jason.server.util.http.HttpRequestUtil;
import com.jason.server.util.http.SocketInputStream;

/**
  * This processor is like the class HttpServer1 in some way.They 
  * parse the request Uri and then decide handle it to servlet processor 
  * or static processor.Also,it parses the request's first line and header.
  * @author lwz
  * @since JDK1.8
  */
public class HttpProcessor
{	
	////////////instance's fields/////////
	
	private HttpServletRequest httpServletRequest;
	private HttpServletResponse httpServletResponse;
	private HttpConnector connector;
	protected MyServletRequest request = new MyServletRequest();
	protected MyServletResponse response;
	/**
	 * Constructor.
	 * Wrap the connector
	 * @param conn
	 */
	public HttpProcessor(HttpConnector conn)
	{
		this.connector = conn;
	}
	
	public HttpProcessor(){}
	
	/**
	 * process the socket,can only handle http protocol right now 
	 * @param socket
	 */
	public void process(Socket socket)
	{
		SocketInputStream input  = null;
		OutputStream output = null;
		try
		{
			input = new SocketInputStream(socket.getInputStream(),2048);
			output = socket.getOutputStream();
			
			//later could use a socket wrapper,to screen lower level detail
			request.setInputStream(input);	//parse until the fields got called
			response = new MyServletResponse(output);
			
			response.setRequest(request);
			
			HttpRequestUtil.parseRequestLine(request);
			HttpRequestUtil.parseHeaders(request);
			
			//wrapper object
			httpServletRequest =new HttpServletRequestWrapper(request);
			httpServletResponse = new HttpServletResponseWrapper(response);
			
			//easy resource mapping ;-)
			//only provide service for servlet & html
			if(httpServletRequest.getRequestURI().startsWith("/servlet/"))	//calling servlet
			{
				//TODO: make processors recycled to avoid GC
				ServletProcessor servletProcessor = new ServletProcessor();
				//servletProcessor.process(request,response);
			}
			else
			{
				StaticResourceProcessor staticProcessor = new StaticResourceProcessor();
				//staticProcessor.process(request,response);
			}
			
		}
		catch(Exception e)
		{
			try {
				socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
}
			
			