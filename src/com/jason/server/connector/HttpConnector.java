package com.jason.server.connector;

import java.net.*;
import java.io.IOException;

/**
  * In this module,the HttpConnector in charge of creating a new thread and building 
  * a serversocket upon it.In addition,this module did not create a stop method for the 
  * application so user have to shutdown the server manually(by closing the commandline
  * or stopping the thread.
  * @author Jason
  * @since JDK1.8
  */
public class HttpConnector implements Runnable
{
	private boolean stopped = false;	//flag
	private String schema = "http"; 	//handle http request
	//default 8080
	private int port = 8080;
	
	public HttpConnector(int port)
	{
		this.port = port;
	}
	
	public HttpConnector(){}
	
	public String getSchema()
	{
		return schema;
	}
	
	@Override
	public void run()
	{
		ServerSocket serverSocket = null;
		try
		{
		 	serverSocket = new ServerSocket(port,1,InetAddress.getByName("127.0.0.1"));//binding
		}
		catch(IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("server socket bind success");
		while(!stopped)
		{
			Socket socket = null;
			try
			{
				socket = serverSocket.accept();
			}
			catch(IOException e)
			{
				continue;
			}
			//Hand this socket off to HttpProcessor
			HttpProcessor processor = new HttpProcessor(this);
			try
			{
				processor.process(socket);
			}
			catch(Exception e)
			{
				try {
					socket.close();
				} catch (IOException e1) {
					
				}
			}
		}//while
	}
	
	public void start()
	{
		Thread thread = new Thread(this);			//set this object as the new thread's target
		thread.start();													   //start new thread
	}
}
		
		 