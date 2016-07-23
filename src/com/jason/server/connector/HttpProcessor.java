package com.jason.server.connector;

import java.net.*;

/**
  * This processor is like the class HttpServer1 in some way.They 
  * parse the request Uri and then decide handle it to servlet processor 
  * or static processor.Also,it parses the request's first line and header.
  * @author Jason
  */

public class HttpProcessor
{
	private HttpRequest request = null;
	private HttpResponse response = null;
	
	public HttpProcessor(HttpConnector conn)
	{
		//TODO: finish the constructor
	}
	
	public HttpProcessor(){}
	
	public void process(Socket socket)
	{
		SocketInputStream input  = null;
		OutputStream output = null;
		try
		{
			input = new SocketInputStream(socket.getInputStream(),2048);
			output = socket.getOutputStream();
			
			request = new HttpRequest(input);
			
			response = new HttpResponse(output);
			
			response.setRequest(request);
			response.setHeader("Server","Jason's Server");
			
			parseRequest(input,output);
			parseHeaders(input);
			
			//snippet below is taken from HttpServer1
			if(request.getUri().startsWith("/servlet/"))	//calling servlet
			{
				ServletProcessor servletProcessor = new ServletProcessor1();
				servletProcessor.process(request,response);
			}
			else			//request for static resource
			{
				StaticResourceProcessor staticProcessor = new StaticResourceProcessor();
				staticProcessor.process(request,response);
			}
			
			socket.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
			
			