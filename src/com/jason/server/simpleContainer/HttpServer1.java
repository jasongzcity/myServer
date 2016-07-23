package com.jason.server.simpleContainer;

import java.io.*;
import java.net.*;
import com.jason.server.simpleHttpServer.HttpServer;

/**
  * This is the server entrance which accepts the 
  * client's request. Written after reading How Tomcat Works.  
  * @author Jason
  */ 

public class HttpServer1 extends HttpServer
{
	
	public static String WEB_ROOT = System.getProperty("user.dir")+File.separator+"webroot";
	private static String SHUTDOWN = "/shutdown";
	
	//default 8080
	//private int port = 8080;
	//private boolean shutdown = false;
	
	public HttpServer1(int port)
	{
		super(port);//call parent's constructor
	}
	
	public HttpServer1(){}
	 
	 @Override
	public void await()
	{
		ServerSocket serverSocket = null;
		try
		{
			serverSocket = new ServerSocket(port,1,InetAddress.getByName("127.0.0.1"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
		while(!shutdown)
		{
			Socket socket = null;
			InputStream in = null;
			OutputStream out = null;
			try
			{
				socket = serverSocket.accept();//blocking thread here
				in = socket.getInputStream();
				out = socket.getOutputStream();
				
				//when having request from client,create request object
				Request request = new Request(in);
				request.parse();
				
				//create response
				Response response = new Response(out); 
				response.setRequest(request);
				
				if(request.getUri().startsWith("/servlet/"))	//calling servlet
				{
					ServletProcessor1 servletProcessor = new ServletProcessor1();
					servletProcessor.process(request,response);
				}
				else			//request for static resource
				{
					StaticResourceProcessor staticProcessor = new StaticResourceProcessor();
					staticProcessor.process(request,response);
				}
				socket.close();
				
				//if client sends shutdown command,change the flag and shutdown server
				shutdown = request.getUri().equals(SHUTDOWN);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				continue;
			}
		}//while			
	}
	
	
	public static void main(String[] args)
	{
		HttpServer1 server = null;
		if(args.length>0)//set port in commandline
		{
			int port = Integer.parseInt(args[0]);//accept port as the first parameter 
			server = new HttpServer1(port);
		}
		else
		{
			server = new HttpServer1();
		}
		server.await();//waiting request
	}
}
