package com.jason.server.simpleHttpServer;

import java.io.*;
import java.net.*;

/**
  * This is the server entrance which accepts the 
  * client's request. Written after reading How Tomcat Works.  
  * @author Jason
  */ 

public class HttpServer
{
	//WEBROOT is the directory contains HTMLs and other files
	public static String WEB_ROOT = System.getProperty("user.dir")+File.separator+"webroot";
	//matches the shutdown command
	private static String SHUTDOWN = "/shutdown";
	
	protected boolean shutdown = false;
	//default 8080
	protected int port = 8080;
	
	public HttpServer(int port)
	{
		this.port = port;
	}
	
	public HttpServer()
	{}

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
				response.sendStaticResource();
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
		HttpServer server = null;
		if(args.length>0)//set port in commandline
		{
			int port = Integer.parseInt(args[0]);//accept port as the first parameter 
			server = new HttpServer(port);
		}
		else
		{
			server = new HttpServer();
		}
		server.await();//waiting request
	}
}
