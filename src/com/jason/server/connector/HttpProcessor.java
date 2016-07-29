package com.jason.server.connector;

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.jason.server.simpleContainer.ServletProcessor1;
import com.jason.server.simpleContainer.StaticResourceProcessor;
import com.jason.server.util.exception.InvalidRequestException;
import com.jason.server.util.http.HttpRequestLine;
import com.jason.server.util.http.HttpRequestUtil;
import com.jason.server.util.http.SocketInputStream;

/**
  * This processor is like the class HttpServer1 in some way.They 
  * parse the request Uri and then decide handle it to servlet processor 
  * or static processor.Also,it parses the request's first line and header.
  * @author Jason
  * @since JDK1.8
  */
public class HttpProcessor
{	
	////////////instance's fields/////////
	
	HttpServletRequest httpServletRequest = null;
//	private HttpResponse response = null;
	private HttpConnector connector = null;
	
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
			MyServletRequest request = new MyServletRequest();//own-create class to be wrapped
			request.setInputStream(input);
//			response = new HttpResponse(output);
//			
//			response.setRequest(request);
//			response.setHeader("Server","Jason's Server");
			
			HttpRequestUtil.parseRequestLine(input,output,request);
			HttpRequestUtil.parseHeaders(input,request);
			
			httpServletRequest =new HttpServletRequestWrapper(request);
			
			//snippet below is taken from HttpServer1
			if(httpServletRequest.getRequestURI().startsWith("/servlet/"))	//calling servlet
			{
				ServletProcessor1 servletProcessor = new ServletProcessor1();
				//servletProcessor.process(request,response);
			}
			else			//request for static resource
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
			
			